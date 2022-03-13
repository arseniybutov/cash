package ru.crystals.jpos.services.scale;


import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;


public class DivaScaleSvc112Factory  implements JposServiceInstanceFactory
{

    public DivaScaleSvc112Factory() {
    }

    @SuppressWarnings("unchecked")
	public JposServiceInstance createInstance(String s, JposEntry jposentry)
        throws JposException
    {
        JposServiceInstance jposserviceinstance = null;
        try
        {
            Class class1 = Class.forName("ru.crystals.jpos.services.scale.DivaScaleSvc112");
            jposserviceinstance = (JposServiceInstance)class1.newInstance();
            System.out.println("Factory: instance is created");
        }
        catch(ClassNotFoundException classnotfoundexception)
        {
            throw new JposException(104, "DigiScaleSvc112 does not exist!", classnotfoundexception);
        }
        catch(InstantiationException instantiationexception)
        {
            throw new JposException(104, "DigiScaleSvc112 could not be instantiated!", instantiationexception);
        }
        catch(IllegalAccessException illegalaccessexception)
        {
            throw new JposException(104, "DigiScaleSvc112 creation failed!", illegalaccessexception);
        }
        return jposserviceinstance;
    }


}
