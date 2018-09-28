/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitcoinj2;

/**
 *
 * @author wen.s
 */
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;

/**
 *
 * @author User
 */
public class SendingMoney {

    public static void main(String[] args) {

        NetworkParameters params = TestNet3Params.get();
        WalletAppKit kit = new WalletAppKit(params, new File("00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008"), "forwarding-service-testnet");
        kit.startAsync();
        kit.awaitRunning();
       
        Coin value = Coin.parseCoin("0.001");
        Address forwardingAddress = Address.fromBase58(params, "mt7HFgcZNgjKJEMSjKWxD5dpkj88vruCfy");    //mkM11AJe4YyuQBoo58YuGEkj2tmiQIducV      //mt7HFgcZNgjKJEMSjKWxD5dpkj88vruCfy    //00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008
        System.out.println("Send money to: " + forwardingAddress.toString());

        
        try {
            Wallet.SendResult result = kit.wallet().sendCoins(kit.peerGroup(), forwardingAddress, value);
          
            System.out.println("coins sent. transaction hash: " + result.tx.getHashAsString());
            
        } catch (InsufficientMoneyException e) {
            System.out.println("Not enough coins in your wallet. Missing " + e.missing.getValue() + " satoshis are missing (including fees)");
            System.out.println("Send money to: " + kit.wallet().currentReceiveAddress().toString());

            
            ListenableFuture<Coin> balanceFuture = kit.wallet().getBalanceFuture(value, Wallet.BalanceType.AVAILABLE);
            FutureCallback<Coin> callback = new FutureCallback<Coin>() {
                @Override
                public void onSuccess(Coin balance) {
                    System.out.println("coins arrived and the wallet now has enough balance");
                }

                @Override
                public void onFailure(Throwable t) {
                    System.out.println("something went wrong");
                }
            };
            Futures.addCallback(balanceFuture, callback);
        }

    }

}


