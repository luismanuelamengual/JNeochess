
package org.neochess.engine.evaluators;

import org.neochess.engine.Board;
import org.neochess.general.Disposable;

public abstract class Evaluator implements Disposable
{
    public void dispose () {}
    public abstract int evaluate (Board board);
}
