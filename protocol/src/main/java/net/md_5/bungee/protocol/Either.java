package net.md_5.bungee.protocol;

import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Either<L, R>
{

    private final L left;
    private final R right;

    public boolean isLeft()
    {
        return this.left != null;
    }

    public boolean isRight()
    {
        return this.right != null;
    }

    public static <L, R> Either<L, R> left(L left)
    {
        return new Either<>( left, null );
    }

    public static <L, R> Either<L, R> right(R right)
    {
        return new Either<>( null, right );
    }

    public L getLeftOrCompute(Function<R, L> function)
    {
        if ( isLeft() )
        {
            return left;
        } else
        {
            return function.apply( right );
        }
    }

    public R getRightOrCompute(Function<L, R> function)
    {
        if ( isRight() )
        {
            return right;
        } else
        {
            return function.apply( left );
        }
    }
}
