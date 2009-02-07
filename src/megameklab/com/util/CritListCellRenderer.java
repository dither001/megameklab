/*
 * MegaMekLab - Copyright (C) 2009
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

/*
 * Thanks to Lost in space of the Solaris Sunk Works Project for the code snippet and idea.
 */

package megameklab.com.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import megamek.common.AmmoType;
import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.Mounted;
import megamek.common.WeaponType;

public class CritListCellRenderer extends DefaultListCellRenderer {

    private JList list = null;
    private int index = -1;
    private Entity unit = null;
    private boolean useColor = false;
    private CConfig config;
    /**
     *
     */
    private static final long serialVersionUID = 1599368063832366744L;

    public CritListCellRenderer(Entity unit, boolean useColor, CConfig config) {
        this.unit = unit;
        this.useColor = useColor;
        this.config = config;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        this.list = list;
        this.index = index;

        CriticalSlot cs = getCrit();

        if (cs != null) {

            if (cs.getType() == CriticalSlot.TYPE_SYSTEM && useColor) {
                try {
                    label.setBackground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_SYSTEMS_BACKGROUND))));
                } catch (Exception ex) {
                }
                try {
                    label.setForeground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_SYSTEMS_FOREGROUND))));
                } catch (Exception ex) {
                }

            } else if (cs.getMount() != null) {

                Mounted mount = cs.getMount();

                if (useColor) {
                    String name = UnitUtil.getCritName(unit, mount.getType());

                    if (mount.isRearMounted()) {
                        name += "(R)";
                    }
                    label.setText(name);

                    if (mount.getType() instanceof WeaponType) {
                        try {
                            label.setBackground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_WEAPONS_BACKGROUND))));
                        } catch (Exception ex) {
                        }
                        try {
                            label.setForeground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_WEAPONS_FOREGROUND))));
                        } catch (Exception ex) {
                        }
                    } else if (mount.getType() instanceof AmmoType) {
                        try {
                            label.setBackground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_AMMO_BACKGROUND))));
                        } catch (Exception ex) {
                        }
                        try {
                            label.setForeground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_AMMO_FOREGROUND))));
                        } catch (Exception ex) {
                        }

                    } else {
                        try {
                            label.setBackground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_EQUIPMENT_BACKGROUND))));
                        } catch (Exception ex) {
                        }
                        try {
                            label.setForeground(Color.getColor("", Integer.parseInt(config.getParam(CConfig.CONFIG_EQUIPMENT_FOREGROUND))));
                        } catch (Exception ex) {
                        }
                    }
                }
                label.setToolTipText(UnitUtil.getToolTipInfo(unit, mount.getType()));
            }

        }


        return label;
    }

    private CriticalSlot getCrit() {
        int slot = index;
        int location = getCritLocation();
        CriticalSlot crit = null;
        if (slot >= 0 && slot < unit.getNumberOfCriticals(location)) {
            crit = unit.getCritical(location, slot);
        }

        return crit;
    }

    private int getCritLocation() {
        return Integer.parseInt(list.getName());
    }

}