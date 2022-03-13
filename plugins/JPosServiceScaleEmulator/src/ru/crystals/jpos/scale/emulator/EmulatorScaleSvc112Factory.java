package ru.crystals.jpos.scale.emulator;

import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;


public class EmulatorScaleSvc112Factory  implements JposServiceInstanceFactory
{

    public EmulatorScaleSvc112Factory()
    {
    }

    @SuppressWarnings("unchecked")
	public JposServiceInstance createInstance(String s, JposEntry jposentry)
        throws JposException
    {
        JposServiceInstance jposserviceinstance = null;
        try
        {
            Class class1 = Class.forName("ru.crystals.jpos.scale.emulator.EmulatorScaleSvc112");
            jposserviceinstance = (JposServiceInstance)class1.newInstance();
        }
        catch(ClassNotFoundException classnotfoundexception)
        {
            throw new JposException(104, "EmulatorScaleSvc112 does not exist!", classnotfoundexception);
        }
        catch(InstantiationException instantiationexception)
        {
            throw new JposException(104, "EmulatorScaleSvc112 could not be instantiated!", instantiationexception);
        }
        catch(IllegalAccessException illegalaccessexception)
        {
            throw new JposException(104, "EmulatorScaleSvc112 creation failed!", illegalaccessexception);
        }
        return jposserviceinstance;
    }


}
