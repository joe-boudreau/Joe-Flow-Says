package test;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Joe Flow
 */
public class testStuff {


public void main(String[] args) {
    
    for (Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()){
        System.out.println(f.getName());
    }
}    
}
