/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mo.common.impl.provider;

import org.ccsds.moims.mo.common.login.body.LoginResponse;
import org.ccsds.moims.mo.common.login.structures.Profile;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.provider.MALInteraction;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.LongList;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andreea Pirvulescu
 */
public class LoginProviderServiceImplTest {
    
    private static LoginProviderServiceImpl instance;
    MALInteraction mali = null;
    private static Profile prfl;
    private static String string;
    
    @BeforeClass
    public static void setUpClass() throws MALException {
       instance = new LoginProviderServiceImpl();
       prfl = new Profile();
       string = "test";
    }
    
    /**
     * Login operation
     */
    
    /**
     * Test #1: profile argument is null
     * @throws java.lang.Exception
     */
    @Test
    public void testLoginNullArg() throws Exception {
        
        LoginResponse expResult = null;      
        try {
            LoginResponse result = instance.login(null, string, mali);
            assertEquals(expResult, result);
        } catch(IllegalArgumentException e) {
        }
    }
    
    /**
     * Test #2: username is *
     * @throws java.lang.Exception
     */
    @Test
    public void testLoginUsername() throws Exception {
        
        prfl.setUsername(new Identifier("*"));
        prfl.setRole(new Long(1));
        
        LoginResponse expResult = null;
        try {
            LoginResponse result = instance.login(prfl, string, mali);
            assertEquals(expResult, result);
        } catch(MALInteractionException e) {
        }
    }
    
    /**
     * Test #3: role is null
     * @throws java.lang.Exception
     */
    @Test
    public void testLoginNullRole() throws Exception {
        
        prfl.setUsername(new Identifier("test"));
        prfl.setRole(null);
       
        LoginResponse expResult = null;
        try {
            LoginResponse result = instance.login(prfl, string, mali);
            assertEquals(expResult, result);
        } catch(MALInteractionException e) {
        }
    }
    
    /**
     * Test #4: invalid username and role
     * @throws java.lang.Exception
     */
    @Test
    public void testLoginIncorrectUser() throws Exception {
        
        prfl.setUsername(new Identifier("wrong"));
        prfl.setRole(new Long(0));       
        
        Blob expResult = null;
        try {
            LoginResponse result = instance.login(prfl, string, mali);
            assertEquals(expResult, result);
        } catch(MALInteractionException e) {
        }
    }      

    /**
     * ListRoles operation
     */
    
    /**
     * Test #1: username is null
     * @throws java.lang.Exception
     */
    @Test
    public void testListRolesNullUsername() throws Exception {
        
        prfl.setUsername(null);
        
        try {
            LongList result = instance.listRoles(prfl.getUsername(), string, mali);
        } catch (IllegalArgumentException e){
        }   
    }
    
    /**
     * Test #2: username is *
     * @throws java.lang.Exception
     */
    @Test
    public void testListRolesUsername() throws Exception {
        
        prfl.setUsername(new Identifier("*"));
        
        try {
            LongList result = instance.listRoles(prfl.getUsername(), string, mali);
        } catch (MALInteractionException e){
        }     
    }
    
    /**
     * Test #3: user is incorrect
     * @throws java.lang.Exception
     */
    @Test
    public void testListRolesNullRole() throws Exception {
        
        prfl.setUsername(new Identifier("wrong"));
        prfl.setRole(new Long(1));
        
        try {
            LongList result = instance.listRoles(prfl.getUsername(), string, mali);
        } catch (MALInteractionException e){
        }       
    }

    /**
     * Test #4: user exists
     * @throws java.lang.Exception
     */
    @Test
    public void testListRoles() throws Exception {
        
        prfl.setUsername(new Identifier("test"));
        
        LongList expResult = new LongList();
        expResult.add(new Long(2));
        LongList result = instance.listRoles(prfl.getUsername(), string, mali);
        assertEquals(expResult, result);
    }
    
}
