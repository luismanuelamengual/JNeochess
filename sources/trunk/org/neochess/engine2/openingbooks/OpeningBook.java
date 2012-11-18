
package org.neochess.engine2.openingbooks;

import org.neochess.engine2.Board;
import org.neochess.general.Disposable;

public abstract class OpeningBook implements Disposable
{
    public void dispose () {}
    public abstract int getMove (Board board);
}
