package com.wzj.sign;

import org.junit.Test;
import static org.junit.Assert.*;

public class AccountTest {

    @Test
    public void testAccountCreation() {
        Account account = new Account("123456", "openid123", "116.40", "39.90");
        assertEquals("123456", account.getUin());
        assertEquals("openid123", account.getOpenid());
        assertEquals("116.40", account.getLongitude());
        assertEquals("39.90", account.getLatitude());
    }

    @Test
    public void testAccountSetters() {
        Account account = new Account("", "", "", "");
        account.setUin("789012");
        account.setOpenid("newopenid");
        account.setLongitude("121.47");
        account.setLatitude("31.23");
        
        assertEquals("789012", account.getUin());
        assertEquals("newopenid", account.getOpenid());
        assertEquals("121.47", account.getLongitude());
        assertEquals("31.23", account.getLatitude());
    }

    @Test
    public void testToConfigString() {
        Account account = new Account("123456", "openid123", "116.40", "39.90");
        String config = account.toConfigString();
        assertEquals("openid123,116.40,39.90", config);
    }

    @Test
    public void testToConfigStringWithEmptyCoordinates() {
        Account account = new Account("123456", "openid123", "", "");
        String config = account.toConfigString();
        assertEquals("openid123,0,0", config);
    }

    @Test
    public void testFromConfigString() {
        String config = "openid123,116.40,39.90";
        Account account = Account.fromConfigString("123456", config);
        
        assertEquals("123456", account.getUin());
        assertEquals("openid123", account.getOpenid());
        assertEquals("116.40", account.getLongitude());
        assertEquals("39.90", account.getLatitude());
    }

    @Test
    public void testFromConfigStringWithMissingCoordinates() {
        String config = "openid123";
        Account account = Account.fromConfigString("123456", config);
        
        assertEquals("123456", account.getUin());
        assertEquals("openid123", account.getOpenid());
        assertEquals("", account.getLongitude());
        assertEquals("", account.getLatitude());
    }
}