/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitcoinj2;
import org.bitcoinj.core.*;




/**
 *
 * @author user
 */
public class Bitcoinj2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        //use test net by defaut
        String net = "test";
        
        if(args[0].equals("prod"))
        {
            net = args[0];
            System.out.println("Using" + net + "network");
        }
        
        //create a new EC Key
        ECKey key = new ECKey();
        
        //,....and look at the key pair
        System.out.println("We created key :\n "+key);
        
        //either test or production net are posssible
        final NetworkParameters netParamms;
        
        if (net.equals("prod"))
        {
            netParamms = NetworkParameters.prodNet();
            
        }else
        {
            netParamms = NetworkParameters.testNet();
        }
        
        //get valid bitcoin address from public key
        Address addressFromKey = key.toAddress(netParamms);
        System.out.println("on the " + net + " network we can use this address:\n" + addressFromKey);
        
    }
    
}
