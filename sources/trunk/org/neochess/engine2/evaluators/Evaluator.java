
package org.neochess.engine2.evaluators;

import org.neochess.engine2.Board;
import org.neochess.general.Disposable;

public abstract class Evaluator implements Disposable
{
    public void dispose () {}
    public abstract int evaluate (Board board);
}
