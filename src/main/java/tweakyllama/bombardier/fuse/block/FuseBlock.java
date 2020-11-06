package tweakyllama.bombardier.fuse.block;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tweakyllama.bombardier.Bombardier;
import tweakyllama.bombardier.base.handler.RegistryHandler;
import tweakyllama.bombardier.fuse.Fuse;
import tweakyllama.bombardier.fuse.api.FuseIgnitable;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("deprecation")
public class FuseBlock extends Block {
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.REDSTONE_NORTH;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.REDSTONE_EAST;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.REDSTONE_SOUTH;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.REDSTONE_WEST;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));

    private static final List<EnumProperty<RedstoneSide>> SIDES = Arrays.asList(NORTH, EAST, SOUTH, WEST);
    private static final VoxelShape BASE_SHAPE = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
    private static final Map<Direction, VoxelShape> SIDE_TO_SHAPE = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D),
            Direction.SOUTH, Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D),
            Direction.EAST, Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D),
            Direction.WEST, Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)
    ));
    private static final Map<Direction, VoxelShape> SIDE_TO_ASCENDING_SHAPE = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, VoxelShapes.or(SIDE_TO_SHAPE.get(Direction.NORTH), Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)),
            Direction.SOUTH, VoxelShapes.or(SIDE_TO_SHAPE.get(Direction.SOUTH), Block.makeCuboidShape(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)),
            Direction.EAST, VoxelShapes.or(SIDE_TO_SHAPE.get(Direction.EAST), Block.makeCuboidShape(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)),
            Direction.WEST, VoxelShapes.or(SIDE_TO_SHAPE.get(Direction.WEST), Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))
    ));
    private final Map<BlockState, VoxelShape> stateToShapeMap = Maps.newHashMap();

    public FuseBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(NORTH, RedstoneSide.NONE)
                .with(EAST, RedstoneSide.NONE)
                .with(SOUTH, RedstoneSide.NONE)
                .with(WEST, RedstoneSide.NONE)
                .with(LIT, false)
        );
        for (BlockState state : this.getStateContainer().getValidStates()) {
            if (!state.get(LIT)) {
                this.stateToShapeMap.put(state, this.getShapeForState(state));
            }
        }
        // currently register a block item to make placement logic easier
        RegistryHandler.registerBlock(this, "fuse", true);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, LIT);
    }

    /**
     * Precalculates the voxel shape for a fuse block for a given state
     */
    private VoxelShape getShapeForState(BlockState state) {
        VoxelShape voxelshape = BASE_SHAPE;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = state.get(FACING_PROPERTY_MAP.get(direction));
            if (redstoneside == RedstoneSide.SIDE) {
                voxelshape = VoxelShapes.or(voxelshape, SIDE_TO_SHAPE.get(direction));
            } else if (redstoneside == RedstoneSide.UP) {
                voxelshape = VoxelShapes.or(voxelshape, SIDE_TO_ASCENDING_SHAPE.get(direction));
            }
        }
        return voxelshape;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return this.stateToShapeMap.get(state.with(LIT, false));
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Bombardier.LOGGER.info("getting initial state for placement");
        return this.getUpdatedState(context.getWorld(), this.getDefaultState(), context.getPos());
    }

    private BlockState getUpdatedState(IBlockReader world, BlockState state, BlockPos pos) {
//        return recalculateFacingState(world, state, pos);
//        boolean previouslyInvalid = areAllSidesNotConnected(state);
//        state = this.recalculateFacingState(world, this.getDefaultState().with(LIT, state.get(LIT)), pos);
//        if (!previouslyInvalid || !areAllSidesNotConnected(state)) {
//            boolean northConnected = state.get(NORTH) != RedstoneSide.NONE;
//            boolean southConnected = state.get(SOUTH) != RedstoneSide.NONE;
//            boolean eastConnected = state.get(EAST) != RedstoneSide.NONE;
//            boolean westConnected = state.get(WEST) != RedstoneSide.NONE;
//            boolean notNorthSouth = !northConnected && !southConnected;
//            boolean notEastWest = !eastConnected && !westConnected;
//
//            if (!westConnected && notNorthSouth)
//                state = state.with(WEST, RedstoneSide.SIDE);
//
//            if (!eastConnected && notNorthSouth)
//                state = state.with(EAST, RedstoneSide.SIDE);
//
//            if (!northConnected && notEastWest)
//                state = state.with(NORTH, RedstoneSide.SIDE);
//
//            if (!southConnected && notEastWest)
//                state = state.with(SOUTH, RedstoneSide.SIDE);
//
//        }
//        return state;
//        boolean nonNormalCubeAbove = !world.getBlockState(pos.up()).isNormalCube(world, pos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide side = this.getSideConnection(world, state, pos, direction);
            state = state.with(FACING_PROPERTY_MAP.get(direction), side);
        }
        return state;
    }

    private RedstoneSide getSideConnection(IBlockReader world, BlockState state, BlockPos pos, Direction direction) {
        BlockPos offset = pos.offset(direction);
        BlockState offsetState = world.getBlockState(offset);

        if (!canConnectTo(offsetState)) {
            // TODO handle upwards connection
            return RedstoneSide.NONE;
        } else {
            return RedstoneSide.SIDE;
        }

    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        Bombardier.LOGGER.info("update post placement");
        if (facing == Direction.DOWN) {
            return stateIn;
        } else if (facing == Direction.UP) {
            return this.getUpdatedState(worldIn, stateIn, currentPos);
        } else {
            return this.getUpdatedState(worldIn, stateIn, currentPos);
//            RedstoneSide redstoneSide = this.getSide(worldIn, currentPos, facing);
//            if (redstoneSide.func_235921_b_() == stateIn.get(FACING_PROPERTY_MAP.get(facing)).func_235921_b_() && !areAllSidesValid(stateIn)) {
//                return stateIn.with(FACING_PROPERTY_MAP.get(facing), redstoneSide);
//            } else {
    //            return this.getUpdatedState(
    //                    worldIn,
    //                    this.getDefaultState().with(LIT, stateIn.get(LIT)).with(FACING_PROPERTY_MAP.get(facing), redstoneSide),
    //                    currentPos
    //            );
//            }

        }
    }

    private static boolean isSideConnected(BlockState state, Direction direction) {
        return isSideConnected(state, FACING_PROPERTY_MAP.get(direction));
    }

    private static boolean isSideConnected(BlockState state, EnumProperty<RedstoneSide> side) {
        return state.get(side) != RedstoneSide.NONE;
    }

    private static boolean areAllSidesConnected(BlockState state) {
        return SIDES.stream().allMatch(side -> isSideConnected(state, side));
    }

    private static boolean areAllSidesNotConnected(BlockState state) {
        return SIDES.stream().noneMatch(side -> isSideConnected(state, side));
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockstate = world.getBlockState(blockPos);
        return this.canPlaceOnTopOf(world, blockPos, blockstate);
    }

    public boolean canPlaceOnTopOf(IBlockReader reader, BlockPos pos, BlockState state) {
        return state.isSolidSide(reader, pos, Direction.UP);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        worldIn.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() +0.5,0, 0.2, 0 );
        worldIn.removeBlock(pos, false);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide side = getSideConnection(worldIn, state, pos, direction);
            BlockPos offset = pos.offset(direction);
            switch (side) {
                case UP:
                    ignite(worldIn, offset.up());
                    break;
                case SIDE:
                    // TODO this won't go down slopes
                    ignite(worldIn, offset);
                    break;
                default:
                    break;
            }

        }
    }

    /**
     * Called when the block is ignited
     */
    public void ignite(World world, BlockPos pos) {
        Bombardier.LOGGER.info("fuse block ignited");
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(LIT, true));
        world.getPendingBlockTicks().scheduleTick(pos, this, Fuse.fuseBurnTime);
    }

    /**
     * Determines if a block can catch fire
     */
    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public void catchFire(BlockState state, World world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        this.ignite(world, pos);
    }

    /**
     * Checks if the fuse can connect to a block state
     */
    public boolean canConnectTo(BlockState state) {
        return state.isIn(this) || state.getBlock() instanceof FuseIgnitable;
    }

    /**
     * Called when the block is activated
     * If block activated with a flint and steel or a fire charge then ignite
     */
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (state.get(LIT)) {
            return ActionResultType.PASS;
        }
        ItemStack itemstack = player.getHeldItem(handIn);
        Item item = itemstack.getItem();
        if (item != Items.FLINT_AND_STEEL && item != Items.FIRE_CHARGE) {
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        } else {
            this.ignite(worldIn, pos);
            if (!player.isCreative()) {
                if (item == Items.FLINT_AND_STEEL) {
                    itemstack.damageItem(1, player, (player1) -> player1.sendBreakAnimation(handIn));
                } else {
                    itemstack.shrink(1);
                }
            }

            return worldIn.isRemote ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.isIn(state.getBlock()) && !worldIn.isRemote) {
            for (Direction direction : Direction.Plane.VERTICAL) {
                worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
            }
            this.updateNeighboursStateChange(worldIn, pos);
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.isIn(newState.getBlock())) {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            if (!worldIn.isRemote) {
                for (Direction direction : Direction.values()) {
                    worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
                }

                this.updateNeighboursStateChange(worldIn, pos);
            }
        }
    }

    public void updateNeighboursStateChange(World world, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.notifyFuseNeighboursOfStateChange(world, pos.offset(direction));
        }
        for (Direction direction1 : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.offset(direction1);
            if (world.getBlockState(blockpos).isNormalCube(world, blockpos)) {
                this.notifyFuseNeighboursOfStateChange(world, blockpos.up());
            } else {
                this.notifyFuseNeighboursOfStateChange(world, blockpos.down());
            }
        }

    }

    private void notifyFuseNeighboursOfStateChange(World world, BlockPos pos) {
        if (world.getBlockState(pos).isIn(this)) {
            world.notifyNeighborsOfStateChange(pos, this);

            for (Direction direction : Direction.values()) {
                world.notifyNeighborsOfStateChange(pos.offset(direction), this);
            }
        }
    }


    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!state.get(LIT)) {
            BlockState otherState = worldIn.getBlockState(fromPos);
            if (otherState.isIn(Blocks.FIRE)) {
                ignite(worldIn, pos);
            }
        }
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.get(LIT)) {
            float x = pos.getX() + 0.2f + rand.nextFloat() * 0.6f;
            float y = pos.getY() + 0.2f;
            float z = pos.getZ() + 0.2f + rand.nextFloat() * 0.6f;
            worldIn.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            worldIn.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        }
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     * fine.
     */
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rot) {
        switch (rot) {
            case CLOCKWISE_180:
                return state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
            case COUNTERCLOCKWISE_90:
                return state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
            case CLOCKWISE_90:
                return state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
            default:
                return state;
        }
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        switch (mirrorIn) {
            case LEFT_RIGHT:
                return state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
            case FRONT_BACK:
                return state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
            default:
                return super.mirror(state, mirrorIn);
        }
    }

}
