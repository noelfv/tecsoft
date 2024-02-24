/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.tester.beans.FormatterTest;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class TreeUtil {

    private static final Logger log = Logger.getLogger(TreeUtil.class);

    /**
     * Un TreeModel a partir de un InternalFormat que es obtenido a partir del
     * objeto test. Este TreeModel se puede colocar directamente sobre un JTree
     * @param test Es el objeto que contiene los FI's. Pudiendo ser el generado
     * luego de una prueba o FI por probar.
     * @param isGenerated Indica se se debe mostrar el FI generado (true), o el
     * FI por probar (false)
     * @return Un objeto TreeModel para ser usado directamente como la informacion
     * a mostrar en un JTree.
     */
    public static TreeModel getTreeModelFromFormatterTest(FormatterTest test, boolean isGenerated) {
        InternalFormat intFmt = isGenerated? test.getIntFormatGenerated() : test.getIntFormatTest();
        
        return getTreeModelFromIntFormat(intFmt,
                "Test-ID=" + test.getId() + ", Format-id=" + test.getFormatId());
    }
    
    /**
     * Crea un objeto JTreeModel y cuyos datos son obtenidos a partir de un FI.
     * Debido a que un FI contiene campos como niveles anidados, la informacion
     * a mostrar en el elemento raiz se recibe como parametro.
     * @param intFmt El IF desde donde se obtendra la informaciona mostrar en el
     * JTreeModel.
     * @param infoRoot La informaciona mostrar en el elemento raiz.
     * @return Un objeto TreeModel para ser usado directamente como la informacion
     * a mostrar en un JTree.
     */
    public static TreeModel getTreeModelFromIntFormat(InternalFormat intFmt, String infoRoot) {
        DefaultTreeModel raiz;
        DefaultMutableTreeNode padre;
        
        padre = new DefaultMutableTreeNode(infoRoot);
        
        addChildToParent(intFmt, padre);
        raiz = new DefaultTreeModel(padre);

        return raiz;
    }

    private static void addChildToParent(InternalFormat intFmt, DefaultMutableTreeNode padre) {
        DefaultMutableTreeNode hijo;
        List<InternalField> fields;
        
        fields = intFmt.getInternalFieldsAsList();
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i) instanceof InternalFormat) {
                log.debug("Intformat interno encontrado");
                hijo = new DefaultMutableTreeNode("ID="+fields.get(i).getId());
                addChildToParent((InternalFormat) fields.get(i), hijo);
            } else {
                hijo = new DefaultMutableTreeNode("ID="+fields.get(i).getId() + ",VALUE=[" + fields.get(i).getValue() + "]");
            }
            padre.add(hijo);
        }
    }
}
