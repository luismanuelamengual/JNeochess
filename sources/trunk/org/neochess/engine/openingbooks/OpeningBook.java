
package org.neochess.engine.openingbooks;

import org.neochess.engine.Board;
import org.neochess.general.Disposable;

public abstract class OpeningBook implements Disposable
{
    public void dispose () {}
    public abstract int getMove (Board board);
}
