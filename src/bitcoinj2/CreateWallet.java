/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitcoinj2;

import java.io.File;
import java.io.IOException;
import org.bitcoinj.core.*;
import org.bitcoinj.wallet.Wallet;

/**
 *
 * @author user
 */
public class CreateWallet {
    public static void main(String[] args) {
        final NetworkParameters netParams = NetworkParameters.testNet();
        Wallet wallet = null;
        final File walletFile = new File("test.wallet");
        
        try
        {
            wallet =new Wallet(netParams);
            //5 time
            for (int i = 0; i < 5; i++) {
                //create key ad add it to thee wallet
                wallet.addKey(new ECKey());                
            }
            
            //save wALLET  contents ti disk
            wallet.saveToFile(walletFile);
                       
        } catch (IOException E)
        {
            System.out.println("Unable to create wallet file");
        }
        
        //fetch the forst key in te wallet directly from the keychain ArrayList
        //ECKey firstKey = wallet.keychain.get(0);
        ECKey firstKey = wallet.freshReceiveKey();
        
        
        //output key
        System.out.println("first key in the wallet : \n" +firstKey);
        
        //and here is the whole wallet
        System.out.println("Complete content of the wallet : \n" +wallet);
        
        //we can use the hash of the publiv key
        //to cehck whetehr te key pair is in this wallet
        if(wallet.isPubKeyHashMine(firstKey.getPubKeyHash()))
        {
            System.out.println("Yep, that's is my key.");
        }else{
            System.out.println("Nope, that key didn't come from this wallet");
        }
    }
}
