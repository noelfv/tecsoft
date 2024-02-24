/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.beans;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class FormatterTest {
    private static final Logger log = Logger.getLogger(FormatterTest.class);
    
    private String id;
    private String description;
    private String formatId;
    private Formatter format;
    private InternalFormat intFormatTest;
    private InternalFormat intFormatExp;
    private InternalFormat intFormatGenerated;
    private VariableByteBuffer frameTest;
    private VariableByteBuffer frameExp;
    private VariableByteBuffer frameGenerated;
    
    public FormatterTest(){
        
    }
    
    public FormatterTest(String id, String formatId, InternalFormat intFormat, 
            VariableByteBuffer frame, String description) {
        this.id = id;
        this.formatId = formatId;
        this.intFormatTest = intFormat;
        this.frameTest = frame;
        this.description = description;
    }
    
    public InternalFormat toIntFormatFromConfig(){
        intFormatGenerated = format.getInternalFormatFromConfig();
        return intFormatGenerated;
    }
    
    public InternalFormat toIntFormat(){
        intFormatGenerated = format.createInternalFormatFromFrame(frameTest);
        return intFormatGenerated;
    }
    
    public VariableByteBuffer toFrameFromConfig(){
        frameGenerated = format.getFrames();
        return frameGenerated;
    }
    
    public VariableByteBuffer toFrame(){
        frameGenerated = format.getFrames(intFormatTest);
        return frameGenerated;
    }
    
    public String getFormatId() {
        return formatId;
    }

    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }

    public VariableByteBuffer getFrameTest() {
        return frameTest;
    }

    public VariableByteBuffer getFrameExp() {
        return frameExp;
    }

    public void setFrameExp(VariableByteBuffer frameExp) {
        this.frameExp = frameExp;
    }

    public void setFrameTest(VariableByteBuffer frameTest) {
        this.frameTest = frameTest;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InternalFormat getIntFormatTest() {
        return intFormatTest;
    }

    public void setIntFormatTest(InternalFormat intFormat) {
        this.intFormatTest = intFormat;
    }

    public InternalFormat getIntFormatExp() {
        return intFormatExp;
    }

    public void setIntFormatExp(InternalFormat intFormatExp) {
        this.intFormatExp = intFormatExp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VariableByteBuffer getFrameGenerated() {
        return frameGenerated;
    }

    public void setFrameGenerated(VariableByteBuffer frameGenerated) {
        this.frameGenerated = frameGenerated;
    }

    public InternalFormat getIntFormatGenerated() {
        return intFormatGenerated;
    }

    public void setIntFormatGenerated(InternalFormat intFormatGenerated) {
        this.intFormatGenerated = intFormatGenerated;
    }

    public Formatter getFormat() {
        return format;
    }

    public void setFormat(Formatter format) {
        this.format = format;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FormatterTest other = (FormatterTest) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.formatId == null) ? (other.formatId != null) : !this.formatId.equals(other.formatId)) {
            return false;
        }
        if (this.format != other.format && (this.format == null || !this.format.equals(other.format))) {
            return false;
        }
        if (this.intFormatTest != other.intFormatTest && (this.intFormatTest == null || !this.intFormatTest.equals(other.intFormatTest))) {
            return false;
        }
        if (this.intFormatExp != other.intFormatExp && (this.intFormatExp == null || !this.intFormatExp.equals(other.intFormatExp))) {
            return false;
        }
        if (this.intFormatGenerated != other.intFormatGenerated && (this.intFormatGenerated == null || !this.intFormatGenerated.equals(other.intFormatGenerated))) {
            return false;
        }
        if (this.frameTest != other.frameTest && (this.frameTest == null || !this.frameTest.equals(other.frameTest))) {
            return false;
        }
        if (this.frameExp != other.frameExp && (this.frameExp == null || !this.frameExp.equals(other.frameExp))) {
            return false;
        }
        if (this.frameGenerated != other.frameGenerated && (this.frameGenerated == null || !this.frameGenerated.equals(other.frameGenerated))) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 97 * hash + (this.formatId != null ? this.formatId.hashCode() : 0);
        hash = 97 * hash + (this.format != null ? this.format.hashCode() : 0);
        hash = 97 * hash + (this.intFormatTest != null ? this.intFormatTest.hashCode() : 0);
        hash = 97 * hash + (this.intFormatExp != null ? this.intFormatExp.hashCode() : 0);
        hash = 97 * hash + (this.intFormatGenerated != null ? this.intFormatGenerated.hashCode() : 0);
        hash = 97 * hash + (this.frameTest != null ? this.frameTest.hashCode() : 0);
        hash = 97 * hash + (this.frameExp != null ? this.frameExp.hashCode() : 0);
        hash = 97 * hash + (this.frameGenerated != null ? this.frameGenerated.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "FormatterTest{" + "id=" + id + ", description=" + description + ", formatId=" + formatId + ", format=" + format + ", intFormatTest=" + intFormatTest + ", intFormatExp=" + intFormatExp + ", intFormatGenerated=" + intFormatGenerated + ", frameTest=" + frameTest + ", frameExp=" + frameExp + ", frameGenerated=" + frameGenerated + '}';
    }
}
