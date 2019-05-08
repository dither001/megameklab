/*
 * MegaMekLab
 * Copyright (C) 2019 The MegaMek Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package megameklab.com.ui.view;

import megamek.common.Entity;
import megamek.common.EntityWeightClass;
import megamek.common.ITechManager;
import megamek.common.ITechnology;
import megamek.common.util.EncodeControl;
import megamek.common.verifier.TestSupportVehicle;
import megameklab.com.ui.util.CustomComboBox;
import megameklab.com.ui.util.TechComboBox;
import megameklab.com.ui.view.listeners.SVBuildListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Chassis panel for support vehicles
 */
public class SVChassisView extends BuildView implements ActionListener, ChangeListener {

    List<SVBuildListener> listeners = new CopyOnWriteArrayList<>();
    public void addListener(SVBuildListener l) {
        listeners.add(l);
    }
    public void removeListener(SVBuildListener l) {
        listeners.remove(l);
    }

    /** Subset of possible types that does not include those that are not yet supported */
    private final List<TestSupportVehicle.SVType> SV_TYPES = Arrays.stream(TestSupportVehicle.SVType.values())
            .filter(t -> !t.equals(TestSupportVehicle.SVType.AIRSHIP)
                    && !t.equals(TestSupportVehicle.SVType.RAIL)
                    && !t.equals(TestSupportVehicle.SVType.SATELLITE)).collect(Collectors.toList());

    private final SpinnerNumberModel spnTonnageModel = new SpinnerNumberModel(20.0, 5.0, 300.0, 0.5);
    private final SpinnerNumberModel spnTonnageModelSmall = new SpinnerNumberModel(4000.0, 100.0, 4999.0, 1.0);
    private final Map<TestSupportVehicle.SVType, String> typeNames = new EnumMap<>(TestSupportVehicle.SVType.class);

    private final JSpinner spnTonnage = new JSpinner(spnTonnageModel);
    private final JCheckBox chkSmall = new JCheckBox();
    private final CustomComboBox<Integer> cbStructureTechRating = new CustomComboBox<>(ITechnology::getRatingName);
    private final CustomComboBox<TestSupportVehicle.SVType> cbType = new CustomComboBox<>(t -> typeNames.getOrDefault(t, "?"));
    private final TechComboBox<TestSupportVehicle.SVEngine> cbEngine = new TechComboBox<>(e -> e.engine.getEngineName()
            .replaceAll("^\\d+ ", ""));
    private final CustomComboBox<Integer> cbEngineTechRating = new CustomComboBox<>(ITechnology::getRatingName);

    private final ITechManager techManager;
    private boolean handleEvents = true;

    public SVChassisView(ITechManager techManager) {
        super();
        this.techManager = techManager;
        initUI();
    }

    private void initUI() {
        ResourceBundle resourceMap = ResourceBundle.getBundle("megameklab.resources.Views", new EncodeControl()); //$NON-NLS-1$
        for (TestSupportVehicle.SVType t : TestSupportVehicle.SVType.values()) {
            typeNames.put(t, resourceMap.getString("SVType." + t.toString()));
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        add(createLabel(resourceMap.getString("SVChassisView.spnTonnage.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 1;
        setFieldSize(spnTonnage, spinnerSizeLg);
        spnTonnage.setToolTipText(resourceMap.getString("SVChassisView.spnTonnage.tooltip")); //$NON-NLS-1$
        add(spnTonnage, gbc);
        spnTonnage.addChangeListener(this);

        gbc.gridx = 2;
        chkSmall.setText(resourceMap.getString("SVChassisView.chkSmall.text")); //$NON-NLS-1$
        chkSmall.setToolTipText(resourceMap.getString("SVChassisView.chkSmall.tooltip")); //$NON-NLS-1$
        add(chkSmall, gbc);
        chkSmall.addActionListener(this);

        for (int r = ITechnology.RATING_A; r <= ITechnology.RATING_F; r++) {
            cbStructureTechRating.addItem(r);
        }
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy++;
        add(createLabel(resourceMap.getString("SVChassisView.cbStructureTechRating.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        setFieldSize(cbStructureTechRating, spinnerSize);
        cbStructureTechRating.setToolTipText(resourceMap.getString("SVChassisView.cbStructureTechRating.tooltip")); //$NON-NLS-1$
        add(cbStructureTechRating, gbc);
        cbStructureTechRating.addActionListener(this);

        SV_TYPES.forEach(cbType::addItem);
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy++;
        add(createLabel(resourceMap.getString("SVChassisView.cbType.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        setFieldSize(cbType, controlSize);
        cbType.setToolTipText(resourceMap.getString("SVChassisView.cbType.tooltip")); //$NON-NLS-1$
        add(cbType, gbc);
        cbType.addActionListener(this);

        cbEngine.setModel(new DefaultComboBoxModel<>(TestSupportVehicle.SVEngine.values()));
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy++;
        add(createLabel(resourceMap.getString("SVChassisView.cbEngine.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        setFieldSize(cbEngine, controlSize);
        cbEngine.setToolTipText(resourceMap.getString("SVChassisView.cbEngine.tooltip")); //$NON-NLS-1$
        add(cbEngine, gbc);
        cbEngine.addActionListener(this);

        for (int r = ITechnology.RATING_A; r <= ITechnology.RATING_F; r++) {
            cbEngineTechRating.addItem(r);
        }
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy++;
        add(createLabel(resourceMap.getString("SVChassisView.cbEngineTechRating.text"), labelSize), gbc); //$NON-NLS-1$
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        setFieldSize(cbEngineTechRating, spinnerSize);
        cbEngineTechRating.setToolTipText(resourceMap.getString("SVChassisView.cbEngineTechRating.tooltip")); //$NON-NLS-1$
        add(cbEngineTechRating, gbc);
        cbEngineTechRating.addActionListener(this);
    }

    /**
     * @return The current weight value. Small support vees have the value converted from kg to tons.
     */
    public double getTonnage() {
        if (chkSmall.isSelected()) {
            return (double) spnTonnage.getValue() / 1000.0;
        } else {
            return (double) spnTonnage.getValue();
        }
    }

    /**
     * @return The currently selected support vehicle type
     */
    public TestSupportVehicle.SVType getType() {
        return (TestSupportVehicle.SVType) cbType.getSelectedItem();
    }

    /**
     * @return The currently selected structural tech rating, or A if none is selected.
     */
    private int getStructuralTechRating() {
        if (cbStructureTechRating.getSelectedItem() != null) {
            return (Integer) cbStructureTechRating.getSelectedItem();
        } else {
            return ITechnology.RATING_A;
        }
    }

    /**
     * @return The currently selected engine tech rating, or A if none is selected.
     */
    private int getEngineTechRating() {
        if (cbEngineTechRating.getSelectedItem() != null) {
            return (Integer) cbEngineTechRating.getSelectedItem();
        } else {
            return ITechnology.RATING_A;
        }
    }

    /**
     * @return The currently selected engine type, or {@link TestSupportVehicle.SVEngine#COMBUSTION COMBUSTION} if none is selected
     */
    private TestSupportVehicle.SVEngine getEngineType() {
        if (cbEngine.getSelectedItem() != null) {
            return (TestSupportVehicle.SVEngine) cbEngine.getSelectedItem();
        } else {
            return TestSupportVehicle.SVEngine.COMBUSTION;
        }
    }

    public void setFromEntity(Entity entity) {
        handleEvents = false;
        refresh();

        if (entity.getWeightClass() == EntityWeightClass.WEIGHT_SMALL_SUPPORT) {
            chkSmall.setSelected(true);
            spnTonnage.setModel(spnTonnageModelSmall);
            spnTonnageModelSmall.setValue(entity.getWeight() * 1000);
        } else {
            chkSmall.setSelected(false);
            spnTonnage.setModel(spnTonnageModel);
            spnTonnageModel.setValue(entity.getWeight());
        }
        cbEngine.removeAllItems();
        for (TestSupportVehicle.SVEngine engine : TestSupportVehicle.SVEngine.values()) {
            if (techManager.isLegal(engine)
                    && engine.isValidFor(entity)) {
                cbEngine.addItem(engine);
            }
        }
        cbEngine.setSelectedItem(TestSupportVehicle.SVEngine.getEngineType(entity.getEngine()));

        cbStructureTechRating.removeAllItems();
        for (int r = entity.getConstructionTechAdvancement().getTechRating(); r <= ITechnology.RATING_F; r++) {
            if (techManager.isLegal(TestSupportVehicle.TECH_LEVEL_TA[r])) {
                cbStructureTechRating.addItem(r);
            }
        }
        cbStructureTechRating.setSelectedItem(entity.getStructuralTechRating());

        cbType.setSelectedItem(TestSupportVehicle.SVType.getVehicleType(entity));

        // Engine::getTechRating will return the minimum tech rating for the engine type.
        cbEngineTechRating.removeAllItems();
        for (int r = entity.getEngine().getTechRating(); r <= ITechnology.RATING_F; r++) {
            if (techManager.isLegal(TestSupportVehicle.TECH_LEVEL_TA[r])) {
                cbEngineTechRating.addItem(r);
            }
        }
        cbEngineTechRating.setSelectedItem(entity.getEngineTechRating());

        handleEvents = true;
        // If the previously selected items are no longer valid, select the first in the list. This
        // is done with event handlers turned on so the Entity gets changed.
        if (getStructuralTechRating() != entity.getStructuralTechRating()) {
            cbStructureTechRating.setSelectedIndex(0);
        }
        if (getEngineTechRating() != entity.getEngineTechRating()) {
            cbEngine.setSelectedIndex(0);
        }
    }

    public void refresh() {
        refreshTonnage();
    }

    private void refreshTonnage() {
        double max = getType().maxTonnage;
        spnTonnageModel.setMaximum(max);
        if (spnTonnageModel.getNumber().doubleValue() > max) {
            spnTonnageModel.setValue(max);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!handleEvents) {
            return;
        }
        if (e.getSource() == chkSmall) {
            spnTonnage.setModel(chkSmall.isSelected() ?
                spnTonnageModelSmall : spnTonnageModel);
            listeners.forEach(l -> l.tonnageChanged(getTonnage()));
        } else if (e.getSource() == cbType) {
            listeners.forEach(l -> l.typeChanged(getType()));
        } else if (e.getSource() == cbStructureTechRating) {
            listeners.forEach(l -> l.structuralTechRatingChanged(getStructuralTechRating()));
        } else if (e.getSource() == cbEngine) {
            listeners.forEach(l -> l.engineChanged(getEngineType().engine));
        } else if (e.getSource() == cbEngineTechRating) {
            listeners.forEach(l -> l.engineTechRatingChanged(getEngineTechRating()));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!handleEvents) {
            return;
        }
        if (e.getSource() == spnTonnage) {
            listeners.forEach(l -> l.tonnageChanged(getTonnage()));
        }
    }
}