/***********************************************************************
 * Copyright (c) 2003-2006 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/
package org.apache.james.util.mail.mdn;

/**
 * Class <code>Disposition</code> encapsulating
 * disposition information as defined by RFC 2298.
 */
public class Disposition
{
    private DispositionActionMode fieldActionMode;
    private DispositionSendingMode fieldSendingMode;
    private DispositionType fieldDispositionType;
    private DispositionModifier[] fieldDispositionModifiers;

    /**
     * Default Construcor
     */
    private Disposition()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param actionMode
     * @param sendingMode
     * @param type
     */
    public Disposition(DispositionActionMode actionMode, DispositionSendingMode sendingMode, DispositionType type)
    {
        this();
        setActionMode(actionMode);
        setSendingMode(sendingMode);
        setDispositionType(type);
    }

    /**
     * Constructor.
     * 
     * @param actionMode
     * @param sendingMode
     * @param type
     * @param modifiers
     */
    public Disposition(DispositionActionMode actionMode, DispositionSendingMode sendingMode, DispositionType type,
            DispositionModifier[] modifiers)
    {
        this(actionMode, sendingMode, type);
        setDispositionModifiers(modifiers);
    }

    /**
     * Answer the Disposition Mode.
     * 
     * @return Returns the dispostionMode.
     */
    protected DispositionActionMode getActionMode()
    {
        return fieldActionMode;
    }

    /**
     * Set the Disposition Mode.
     * 
     * @param dispostionMode The dispostionMode to set.
     */
    protected void setActionMode(DispositionActionMode dispostionMode)
    {
        fieldActionMode = dispostionMode;
    }

    /**
     * Answer the Disposition Modifiers.
     * 
     * @return Returns the dispostionModifiers.
     */
    protected DispositionModifier[] getDispositionModifiers()
    {
        return fieldDispositionModifiers;
    }

    /**
     * Set the Disposition Modifiers.
     * 
     * @param dispostionModifiers The dispostionModifiers to set.
     */
    protected void setDispositionModifiers(DispositionModifier[] dispostionModifiers)
    {
        fieldDispositionModifiers = dispostionModifiers;
    }

    /**
     * Answer the Disposition Type.
     * 
     * @return Returns the dispostionType.
     */
    protected DispositionType getDispositionType()
    {
        return fieldDispositionType;
    }

    /**
     * Set the Disposition Type.
     * 
     * @param dispostionType The dispostionType to set.
     */
    protected void setDispositionType(DispositionType dispostionType)
    {
        fieldDispositionType = dispostionType;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer(64);
        buffer.append("Disposition: ");
        buffer.append(getActionMode() == null ? "" : getActionMode().toString());
        buffer.append('/');
        buffer.append(getSendingMode() == null ? "" : getSendingMode().toString());
        buffer.append(';');
        buffer.append(getDispositionType() == null ? "" : getDispositionType().toString());
        if (null != getDispositionModifiers()
                && getDispositionModifiers().length > 0)
        {
            buffer.append('/');
            for (int i = 0; i < getDispositionModifiers().length; i++)
            {
                if (i > 0)
                    buffer.append(',');
                buffer.append(getDispositionModifiers()[i]);
            }
        }
        return buffer.toString();
    }

    /**
     * Answer the Sending Mode.
     * 
     * @return Returns the sendingMode.
     */
    protected DispositionSendingMode getSendingMode()
    {
        return fieldSendingMode;
    }

    /**
     * Set the Sending Mode.
     * 
     * @param sendingMode The sendingMode to set.
     */
    protected void setSendingMode(DispositionSendingMode sendingMode)
    {
        fieldSendingMode = sendingMode;
    }
}
