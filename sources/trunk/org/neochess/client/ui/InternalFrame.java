
package org.neochess.client.ui;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.neochess.client.Application;
import org.neochess.general.Disposable;
import org.neochess.util.ResourceUtils;

public class InternalFrame extends JInternalFrame implements InternalFrameListener, Disposable
{
    public InternalFrame ()
    {
        super(null,true, true, false, true);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        setFrameIcon(ResourceUtils.getImageIcon(Application.getInstance().getResourceImagesPath() + "frame.png"));
        addInternalFrameListener(this);
    }
    
    @Override
    public void dispose ()
    {
        removeInternalFrameListener(this);
        super.dispose();
    }

    public boolean close ()
    {
        return close(false);
    }
    
    public boolean close (boolean forced)
    {
        dispose();
        return true;
    }
    
    public void internalFrameClosing(InternalFrameEvent e) { close (); }
    public void internalFrameClosed(InternalFrameEvent e) {}
    public void internalFrameOpened(InternalFrameEvent e) {}
    public void internalFrameIconified(InternalFrameEvent e) {}
    public void internalFrameDeiconified(InternalFrameEvent e) {}
    public void internalFrameActivated(InternalFrameEvent e) {}
    public void internalFrameDeactivated(InternalFrameEvent e) {}
}
