package support;



import java.awt.*;
import java.awt.event.*;



/**
 * AbstractButtons are push buttons support the following states: normal, 
 * mouse over, pressed, disabled, and active.
 *
 * @author  Daniel E. Barbalace
 */

public class AbstractButton extends Component implements
    java.awt.event.ComponentListener,
    java.awt.event.FocusListener,
    java.awt.event.KeyListener,
    java.awt.event.MouseListener,
    java.awt.event.MouseMotionListener
{



/** State of button that is not being pressed or has the mouse over it */
protected static final int STATE_NORMAL = 1;

/** State of button that is not being pressed and has the mouse over it */
protected static final int STATE_OVER = 2;

/** State of button that is being pressed */
protected static final int STATE_PRESSED = 3;



/** Gray used for shadow in default rendering */
protected static final Color MID_GRAY = new Color(130, 130, 130);

/** Thread that fires action events for all buttons */
protected static final Runner runner = new Runner("AbstractButton");

/** Button being held down */
protected static AbstractButton buttonHeld;

/** Used to delay action events when button is first pressed */
protected static boolean firstDelay;

/** Default font used for text */
protected static Font defaultFont = new Font("Dialog", Font.PLAIN, 12);



/** Current button state (e.g., STATE_NORMAL) */
protected int state;

/** Previous button state (e.g., STATE_NORMAL) */
protected int prevState;

/** Is the button disabled */
protected boolean disabled;

/** Is the button active */
protected boolean active;

/** Delay before first action event is fired when button is held down */
protected int initialDelay;

/** Delay between action events when button is held down.  A value of zero means
    do not fire events while button is held down. */
protected int delay;

/** Button's text */
protected String text;

/** Does the button have keyboard focus */
protected boolean hasFocus;

/** Determines whether or not focus is requested or indicated with a dashed
    boarder */
protected boolean acceptFocus;

/** Is a key being held down */
protected boolean keyDown;

/** List of action listeners */
protected ActionListener actionListener;



/**
 * Class initializer.  Sets up thread that fires actions events when a button
 * is held down.
 */

static
{
    buttonHeld = null;
    runner.start();
}



/**
 * Initializes the AbstractButton.
 */

public AbstractButton ()
{
    this (null);
}


/**
 * Initializes the AbstractButton.
 *
 * @param label    button label
 */

public AbstractButton (String label)
{
    // Remember label and make sure it is not null
    text = (label == null) ? "" : label;

    // Set font
    setFont (defaultFont);

    // Set background
    setBackground (Color.lightGray);

    // Initialize state
    state = STATE_NORMAL;
    prevState = STATE_NORMAL;
    delay = 0;
    hasFocus = false;
    acceptFocus = true;
    keyDown = false;

    // Register for important events
    addComponentListener (this);
    addFocusListener (this);
    addKeyListener (this);
    addMouseListener (this);
    addMouseMotionListener (this);

    // Indicate that there are currently no action listeners
    actionListener = null;
}



/**
 * Updates this component.  This method only does one thing: calls <code>paint
 * </code>.
 *
 * @param g    Graphics context
 */

public void update (Graphics g)
{
    paint (g);
}



/**
 * Renders this control.  The button is rendered like a standard Windows button
 * unless a subclass overrides this method.
 *
 * @param g    graphics context
 */

public void paint (Graphics g)
{
    // Get size of control
    Dimension d = getSize();

    // Render pressed buttons
    if ((state == STATE_PRESSED) && (!disabled))
    {
        // Draw border
        g.setColor (Color.black);
        g.drawLine (0, 0, d.width - 1, 0);  // Top
        g.drawLine (0, 0, 0, d.height - 1); // Left

        g.setColor (MID_GRAY);
        g.drawLine (1, 1, d.width - 2, 1);  // Top 2
        g.drawLine (1, 1, 1, d.height - 2); // Left 2

        g.setColor (Color.white);
        g.drawLine (1, d.height - 1, d.width - 1, d.height - 1); // Bottom
        g.drawLine (d.width - 1, 1, d.width - 1, d.height - 1);  // Right

        // Draw background
        g.setColor (getBackground());
        g.fillRect (2, 2, d.width - 3, d.height - 3);
    }
    // Render normal, over, and disabled buttons the same way
    else
    {
        // Draw border
        g.setColor (Color.white);
        g.drawLine (0, 0, d.width - 2, 0);  // Top
        g.drawLine (0, 0, 0, d.height - 2); // Left

        g.setColor (Color.black);
        g.drawLine (0, d.height - 1, d.width - 1, d.height - 1); // Bottom
        g.drawLine (d.width - 1, 0, d.width - 1, d.height - 1);  // Right

        g.setColor (MID_GRAY);
        g.drawLine (1, d.height - 2, d.width - 2, d.height - 2); // Bottom 2
        g.drawLine (d.width - 2, 1, d.width - 2, d.height - 2);  // Right 2

        // Draw background
        g.setColor (getBackground());
        g.fillRect (1, 1, d.width - 3, d.height - 3);
    }

    // Get font
    Font font = getFont();

    // Get font metrics
    FontMetrics fm = getToolkit().getFontMetrics(font);
    int width = fm.stringWidth(text);
    int height = fm.getAscent();

    // HACK: Fudge factor to compensate for inaccurate return values of
    // Font.getAscent and Font.getMaxAscent
    height -= 3;

    // Determine where to draw text
    int baseline = (d.height + height) >> 1;
    int x = (d.width - width) >> 1;

    // Offset if pressed
    if ((state == STATE_PRESSED) && (!disabled))
    {
        baseline++;
        x++;
    }

    // Draw text
    g.setColor (disabled ? MID_GRAY : getForeground());
    g.drawString (text, x, baseline);

    // Draw a focus border
    if (hasFocus)
    {
        g.setColor (Color.black);

        final int i = 3;

        for (x = i; x < d.width - i; x += 2)
        {
            g.drawLine (x, i, x, i);
            g.drawLine (x, d.height - i - 1, x, d.height - i - 1);
        }

        for (int y = i; y < d.height - i; y += 2)
        {
            g.drawLine (i, y, i, y);
            g.drawLine (d.width - i - 1, y, d.width - i - 1, y);
        }
    }
}



/**
 * Gets the preferred size of this button.
 *
 * @return a size just large enough to fit the button with it's current text
 */

public Dimension getPreferredSize ()
{
    // Get font
    Font font = getFont();

    // Get font metrics
    FontMetrics fm = getToolkit().getFontMetrics(font);
    int width = fm.stringWidth(text);
    int height = fm.getAscent();

    // HACK: Fudge factor to compensate for inaccurate return values of
    // Font.getAscent and Font.getMaxAscent
    height -= 3;

    // Determine size
    return new Dimension (width + 12, height + 12);
}



/**
 * Sets this button's text.
 *
 * @param text    button's text
 */

public void setText (String text)
{
    this.text = text;
    repaint();
}



/**
 * Gets this button's text.
 *
 * @return this button's text
 */

public String getText ()
{
    return text;
}



/**
 * Specifies whether or not to indicate keyboard focus with a dashed boarder.
 *
 * @param acceptFocus    If true, focus will be indicated.  Otherwise, focus
 *                         will not be visually shown.
 */

public void setAcceptFocus (boolean acceptFocus)
{
    this.acceptFocus = acceptFocus;
    repaint();
}



/**
 * Determines whether or not keyboard focus is indicated with a dashed boarder.
 *
 * @return True, if focus will be indicated.  Otherwise, false.
 */

public boolean getAcceptFocus ()
{
    return acceptFocus;
}



/**
 * Determines whether or not this button is enabled.  If the button is enabled,
 * it will have the "normal" appearance and can be pressed.  Otherwise, the
 * button will have the "disabled" appearance (usually grayed out).
 *
 * @return True, if the button is enabled.  False, otherwise.
 */

public boolean getEnabled ()
{
    return !disabled;
}



/**
 * Enables or disables this button.  If <code>enabled</code> is true, the button
 * will have a "normal" (not disabled) appearance.  Otherwise, the button will
 * have the "disabled" appearance (usually grayed out).
 *
 * @param enabled    should the button be enabled
 */

public void setEnabled (boolean enabled)
{
    // Set disabled
    disabled = !enabled;

    // Repaint control to represent new state
    repaint ();
}



/**
 * Determines whether or not this button is active.  If the button is active,
 * it will have the "active" appearance unless it is disabled.
 *
 * @return True, if the button is active.  False, otherwise.
 */

public boolean getActive ()
{
    return active;
}



/**
 * Activates or deactivates this button.  If <code>active</code> is true, the
 * button will have the "active" appearance.  This method has no affect if the
 * button is disabled.
 *
 * @param active    should the button be active
 */

public void setActive (boolean active)
{
    // Set active
    this.active = active;

    // Repaint control to represent new state
    repaint ();
}



/**
 * Gets the delay between successive action events when this button is held.
 *
 * @return an array of two integers, rc, such that
 * <pre>
 *      rc[0] = the number of milliseconds before the first action event
 *      rc[1] = the number of milliseconds between successive action events
 * </pre>
 */

public int [] getDelay ()
{
    int rc[] = {initialDelay, delay};

    return rc;
}



/**
 * Sets the delay between successive action events when this button is held.
 * A non-positive value for either parameter will disable action events while
 * this button is being held.
 *
 * <p>If this method is called directly or indirectly by an action listener's
 * actionPerformed method while the button is being held, this method will be
 * a no-op unless both parameters are positive.  I.e., a button that is firing
 * action events while being held down cannot stop firing action events until
 * the user releases the button.</p>
 *
 * @param delay0    the number of milliseconds before the first action event
 * @param delay     the number of milliseconds between successive action events
 */

public void setDelay (int delay0, int delay)
{
    // Consider non-positive values to be zero
    if ((delay0 <= 0) || (delay <= 0))
    {
        delay0 = 0;
        delay = 0;
    }

    // Suspend runner if necessary
    if ((buttonHeld == this) && (delay == 0))
    {
        if (Thread.currentThread() == runner)
            return;

        runner.suspend();
        buttonHeld = null;
    }

    // Update state
    this.initialDelay = delay0;
    this.delay = delay;
}



/**
 * Invoked when this button's state has changed.  The button's state can be
 * determined by examining <code>state, disabled,</code> and <code>active
 * </code>.  This method is a no-op, so overriden versions do not need to call
 * this method.
 */

protected void stateChanged ()
{
}



//******************************************************************************
//*** java.awt.event.ComponentListener methods                               ***
//***                                                                        ***
//*** ComponentListener is implemented just to make subclasses simplier.     ***
//******************************************************************************



/**
 * Invoked when component has been resized.  This method does nothing.
 * Overriden versions of this method do not have to call this method.
 */

public void componentResized (ComponentEvent e)
{
}



/**
 * Invoked when component has been moved.  This method does nothing.
 * Overriden versions of this method do not have to call this method.
 */

public void componentMoved (ComponentEvent e)
{
}



/**
 * Invoked when component has been shown.  This method does nothing.
 * Overriden versions of this method do not have to call this method.
 */

public void componentShown (ComponentEvent e)
{
}



/**
 * Invoked when component has been hidden.  This method does nothing.
 * Overriden versions of this method do not have to call this method.
 */

public void componentHidden (ComponentEvent e)
{
}



//******************************************************************************
//*** java.awt.event.FocusListener methods                                   ***
//******************************************************************************



/**
 * Invoked when a component gains the keyboard focus.
 */

public void focusGained (FocusEvent e)
{
    // Flag hasFocus and repaint control to reflect change
    hasFocus = true;
    repaint();
}



/**
 * Invoked when a component loses the keyboard focus.
 */

public void focusLost (FocusEvent e)
{
    // Reset state to previous state
    state = prevState;
    prevState = STATE_NORMAL;

    // Indicate that there is no key release event expected even if a key was
    // pressed
    keyDown = false;

    // Flag hasFocus and repaint control to reflect change
    hasFocus = false;
    repaint();
}



//******************************************************************************
//*** java.awt.event.KeyListener methods                                     ***
//******************************************************************************



/**
 * Invoked when a key has been pressed.
 */

public void keyPressed (KeyEvent e)
{
    // Make sure the source is this object and no key is being held down
    if ((e.getSource() != this) || keyDown)
        return;

    // Flag key as being held down
    keyDown = true;

    // Was the space or enter key pressed
    char ch = e.getKeyChar();

    if ((ch == ' ') || (ch == '\n'))
    {
        // Change state
        prevState = state;
        state = STATE_PRESSED;

        // Repaint control to reflect new state
        repaint();
        
        // Inform subclasses of state change
        stateChanged();
    }
}



/**
 * Invoked when a key has been released.
 */

public void keyReleased (KeyEvent e)
{
    // Make sure the source is this object
    if (e.getSource() != this)
        return;

    // Flag no key being held down
    keyDown = false;

    // Was the space or enter key pressed
    char ch = e.getKeyChar();

    if ((ch == ' ') || (ch == '\n'))
    {
        // Reset previous state
        state = prevState;
        prevState = STATE_NORMAL;

        // Repaint control to reflect new state
        repaint();

        // Inform subclasses of state change
        stateChanged();

        // Are there any listeners
        if ((actionListener != null) && (!disabled))
        {
            // Generate event
            ActionEvent event = new ActionEvent(this, 0, "");

            // Dispatch event
            actionListener.actionPerformed (event);
        }
    }
}



/**
 * Invoked when a key has been typed.
 */

public void keyTyped (KeyEvent e)
{
}



//******************************************************************************
//*** java.awt.event.MouseListener methods                                   ***
//******************************************************************************



/**
 * Invoked when the mouse has been clicked on a component.
 *
 * <p>This method is not used to determine when the component has been
 * clicked because of a bug -- err, ah, feature -- in Java's event
 * dispatching.  The component does not get a mouseClicked if the
 * mouse is moved even one pixel during the clicking.  Human users
 * usually move the mouse slightly when clicking a button.  Also, the
 * user should be able to move the mouse away from the component (to
 * abort the button click) and back over the component (to cancel
 * aborting the button click) before releasing the button and still
 * get a mouse click.  So the logic that determines when the button
 * has been clicked is in mouseRelease.</p>
 */

public void mouseClicked (MouseEvent e)
{
}



/**
 * Invoked when a mouse button has been pressed on a component. 
 */

public void mousePressed (MouseEvent e)
{
    // Make sure the source is this object
    if (e.getSource() == this)
    {
        // Request keyboard focus if desired
        if (acceptFocus && !disabled)
            requestFocus();

        // Change state
        state = STATE_PRESSED;
        prevState = STATE_PRESSED;

        // Repaint control to reflect new state
        repaint();
        
        // Inform subclasses of state change
        stateChanged();

        // If the delay is positive, resume firing thread
        if (delay > 0)
        {
            // Mark button being held and set firstDelay flag
            buttonHeld = this;
            firstDelay = true;

            // Interrupt thread so that the first delay is executed
            runner.interrupt();

            // Resume thread so that the action events may be fired
            runner.resume();
        }
    }
}



/**
 * Invoked when a mouse button has been released on a component.
 */

public void mouseReleased (MouseEvent e)
{
    // Make sure the source is this object
    if (e.getSource() == this)
    {
        // Suspend firing thread if necessary
        if (buttonHeld == this)
        {
            runner.suspend();
            buttonHeld = null;
        }

        // Determine if the mouse is inside the button
        boolean inside = contains(e.getPoint());

        // Check to see if button was clicked (mouse released over button),
        // that the button was pressed, and the button is not disabled
        boolean generateAction =
            inside && (state == STATE_PRESSED) && !disabled;

        // See if mouse is over button
        if (inside)
            // Change state
            state = STATE_OVER;
        else
            // Mouse released when not over button
            state = STATE_NORMAL;

        // Reset previous state
        prevState = STATE_NORMAL;

        // Repaint control to reflect new state
        repaint();

        // Inform subclasses of state change
        stateChanged();

        // Note: The action event is sent after the visual representation of
        // the button has changed in case the action event triggers a change
        // in the visual representation of the button.

        // Send action event if necessary
        if (generateAction && (actionListener != null))
        {
            // Generate event
            ActionEvent event = new ActionEvent(this, 0, "");

            // Dispatch event
            actionListener.actionPerformed (event);
        }
    }
}



/**
 * Invoked when the mouse enters a component.
 */

public void mouseEntered (MouseEvent e)
{
    // Make sure the source is this object
    if (e.getSource() == this)
    {
        // Check to see if button was in STATE_PRESSED state when mouse entered
        if (prevState == STATE_PRESSED)
            // Go back to pressed state
            state = STATE_PRESSED;
        else
            // Change state
            state = STATE_OVER;

        // Repaint control to reflect new state
        repaint();

        // Inform subclasses of state change
        stateChanged();
    }
}



/**
 * Invoked when the mouse exits a component.
 */

public void mouseExited (MouseEvent e)
{
    // Make sure the source is this object
    if (e.getSource() == this)
    {
        // Change state
        state = STATE_NORMAL;

        // Repaint control to reflect new state
        repaint();

        // Inform subclasses of state change
        stateChanged();
    }
}



//******************************************************************************
//** java.awt.event.MouseMotionListener methods                              ***
//******************************************************************************



/**
 * Invoked when a mouse button is pressed on a component and then dragged.
 */

public void mouseDragged (MouseEvent e)
{
    // Check if source is valid
    if (e.getSource() == this)
    {
        // Get mouse coordinates
        Point point = e.getPoint();

        // Was the mouse dragged out of this component
        if (((state == STATE_PRESSED) || (state == STATE_OVER)) &&
            !contains(point))
        {
            // Suspend firing thread if necessary
            if (buttonHeld == this)
            {
                runner.suspend();
                buttonHeld = null;
            }

            // Change state
            state = STATE_NORMAL;

            // Repaint control to reflect new state
            repaint();

            // Inform subclasses of state change
            stateChanged();
        }
        else if ((state == STATE_NORMAL) && contains(point))
        {
            // Go back to previous state
            if (prevState == STATE_PRESSED)
                // Go back to pressed state
                state = STATE_PRESSED;
            else
                // Go to highlighted state
                state = STATE_OVER;

            // If the delay is positive, resume firing thread
            if ((delay > 0) && (state == STATE_PRESSED))
            {
                // Mark button being held and set firstDelay flag
                buttonHeld = this;
                firstDelay = true;

                // Interrupt thread so that the first delay is executed
                runner.interrupt();

                // Resume thread so that the action events may be fired
                runner.resume();
            }

            // Repaint control to reflect new state
            repaint();

            // Inform subclasses of state change
            stateChanged();
        }
    }
}



/**
 * Invoked when the mouse button has been moved on a component (with no buttons
 * no down).
 */

public void mouseMoved (MouseEvent e)
{
}



//******************************************************************************
//*** Support for java.awt.event.ActionListener interface                    ***
//******************************************************************************



/**
 * Adds the specified action listener to receive action events from this button.
 * Action events occur when a user presses and releases the mouse over this
 * button.
 */

public synchronized void addActionListener (ActionListener listener)
{
    actionListener = AWTEventMulticaster.add(actionListener, listener);
}



/**
 * Removes the specified action listener so that it no longer receives action
 * events from this button.  Action events occur when a user presses and
 * releases the mouse over this button. 
 */

public synchronized void removeActionListener (ActionListener listener)
{
    actionListener = AWTEventMulticaster.add(actionListener, listener);
}



//******************************************************************************
//*** Inner class                                                            ***
//******************************************************************************



    /**
     * This class implements the thread that fires action events while a button
     * is held down and the delay is greater than zero.
     */

    protected static class Runner extends Thread
    {
        /**
         * Constructs the Runner.
         *
         * @param threadName    name of the runner thread
         */

        public Runner (String threadName)
        {
            super (threadName);
        }

        /**
         * Keeps sending action events while a button is held, if the delay is
         * positive.  This method is a no-op if it is called by any thread other
         * than <code>runner</code>.
         */

        public void run ()
        {
            // Make sure this method is called only by the class's firing thread
            if (Thread.currentThread() != runner)
                return;

            // Initially, suspend the firing thread
            suspend();

            // Keeping firing action events, forever.  Another method will
            // suspend and resume the firing thread as needed.
            while (true)
            {
                // Note: When this thread is not suspended, buttonHeld must
                // not be null.

                try
                {
                    // Wait a period of time before firing first action event
                    if (firstDelay)
                    {
                        sleep (buttonHeld.initialDelay);
                        firstDelay = false;
                    }

                    // Send action event if necessary
                    if (buttonHeld.actionListener != null)
                    {
                        // Generate event
                        ActionEvent event = new ActionEvent(buttonHeld, 0, "");

                        // Dispatch event
                        buttonHeld.actionListener.actionPerformed (event);
                    }

                    // Wait the designated time
                    sleep (buttonHeld.delay);
                }
                catch (InterruptedException e) {}
            }
        }
    }



}
