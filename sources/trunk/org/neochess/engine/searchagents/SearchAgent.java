
package org.neochess.engine.searchagents;

import org.neochess.engine.Board;
import org.neochess.engine.Move;
import org.neochess.general.Disposable;

public abstract class SearchAgent implements Disposable
{
    public void dispose (){}
    public abstract Move startSearch (Board board, long searchMilliseconds);
    public abstract void stopSearch ();
    public abstract boolean isSearching ();
}
