package ru.crystals.pos.ports.com;

import ru.crystals.bundles.BundleId;
import ru.crystals.bundles.ContextBundle;
import ru.crystals.pos.bl.containers.StatesContainer;
import ru.crystals.pos.bl.listeners.ScenarioListener;
import ru.crystals.pos.ports.PortInterface;
import ru.crystals.pos.ports.configurator.plugin.PortPluginContainerInterface;

import javax.swing.JPanel;
import java.util.List;

@ContextBundle(id = {@BundleId(implemented = PortPluginContainerInterface.class)}, lazy = false)
public class PortComContainer extends StatesContainer implements PortPluginContainerInterface {

    @Override
    public JPanel getVisualPanel() {
        return null;
    }

    @Override
    public List<PortInterface> getPorts() {
        return PortsComLocator.getPorts();
	}

    @Override
    public String getType() {
        return "COM";
    }

    @Override
    public void setScenaryListener(ScenarioListener listener) {
        //
    }
}
