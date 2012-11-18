
package org.neochess.engine2.searchagents;

import org.neochess.engine2.Board;
import org.neochess.general.Disposable;

public abstract class SearchAgent implements Disposable
{
    public void dispose (){}
    public abstract int startSearch (Board board, long searchMilliseconds);
    public abstract void stopSearch ();
    public abstract boolean isSearching ();
}
