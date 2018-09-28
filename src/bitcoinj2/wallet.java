/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitcoinj2;

import java.io.File;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;

/**
 *
 * @author user
 */
public class wallet {
    
    public static void main(String[] args) {
    
     // Figure out which network we should connect to. Each one gets its own set of files.
        NetworkParameters params;
        String filePrefix;
        //if (args[1].equals("testnet")) {
            params = TestNet3Params.get();
            filePrefix = "forwarding-service-testnet";
        //} else if (args[1].equals("regtest")) {
        //    params = RegTestParams.get();
        //    filePrefix = "forwarding-service-regtest";
        //} else {
        //    params = MainNetParams.get();
        //    filePrefix = "forwarding-service";
        //}
        
        
        // Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
        WalletAppKit kit = new WalletAppKit(params, new File("."), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                if (wallet().getKeyChainGroupSize() < 1)
                    wallet().importKey(new ECKey());
            }
        };

        if (params == RegTestParams.get()) {
            // Regression test mode is designed for testing and development only, so there's no public network for it.
            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
            kit.connectToLocalHost();
        }

        // Download the block chain and wait until it's done.
        kit.startAsync();
        kit.awaitRunning();
        System.out.println(kit.wallet());
    
    }
    
    
    
    
}
