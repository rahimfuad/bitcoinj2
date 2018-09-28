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
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ForwardingService demonstrates basic usage of the library. It sits on the network and when it receives coins, simply
 * sends them onwards to an address given on the command line.
 */
public class ReceiveMoney{
    private static Address forwardingAddress;
    private static WalletAppKit kit;

    public static  void main(String[] args) throws Exception {
        // This line makes the log output more compact and easily read, especially when using the JDK log adapter.
//        BriefLogFormatter.init();
//        if (args.length < 1) {
//            System.err.println("Usage: address-to-send-back-to [regtest|testnet]");
//            return;
//        }

        // Figure out which network we should connect to. Each one gets its own set of files.
        NetworkParameters params;
        String filePrefix;
//        if (args.length > 1 && args[1].equals("testnet")) {
            params = TestNet3Params.get();
            filePrefix = "forwarding-service-testnet";
//        } else if (args.length > 1 && args[1].equals("regtest")) {
//            params = RegTestParams.get();
//            filePrefix = "forwarding-service-regtest";
//        } else {
//            params = MainNetParams.get();
//            filePrefix = "forwarding-service";
//        }
        // Parse the address given as the first parameter.
        forwardingAddress = Address.fromBase58(params, "mkM11AJe4YyuQBoo58YuGEkj2tmiQIducV");

        // Start up a basic app using a class that automates some boilerplate.
        kit = new WalletAppKit(params, new File("00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008"), filePrefix);

        if (params == RegTestParams.get()) {
            // Regression test mode is designed for testing and development only, so there's no public network for it.
            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
            kit.connectToLocalHost();
        }

        // Download the block chain and wait until it's done.
        kit.startAsync();
        kit.awaitRunning();

        // We want to know when we receive money.
        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public  void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                // Runs in the dedicated "user thread" (see bitcoinj docs for more info on this).
                //
                // The transaction "tx" can either be pending, or included into a block (we didn't see the broadcast).
                
                Coin value = tx.getValueSentToMe(w);
                System.out.println("Received tx for " + value.toFriendlyString() + ": " + tx);
                System.out.println("Transaction will be forwarded after it confirms.");
                // Wait until it's made it into the block chain (may run immediately if it's already there).
                //
                // For this dummy app of course, we could just forward the unconfirmed transaction. If it were
                // to be double spent, no harm done. Wallet.allowSpendingUnconfirmedTransactions() would have to
                // be called in onSetupCompleted() above. But we don't do that here to demonstrate the more common
                // case of waiting for a block.
                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
                    @Override
                    public  void onSuccess(TransactionConfidence result) {
                        forwardCoins(tx);
                    }

                    @Override
                    public  void onFailure(Throwable t) {
                        // This kind of future can't fail, just rethrow in case something weird happens.
                        throw new RuntimeException(t);
                    }
                });
            }
        });

        Address sendToAddress = kit.wallet().currentReceiveKey().toAddress(params);
        System.out.println("Send coins to: " + sendToAddress);
        System.out.println("Waiting for coins to arrive. Press Ctrl-C to quit.");

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignored) {}
    }

    private static  void forwardCoins(Transaction tx) {
        try {
            Coin value = tx.getValueSentToMe(kit.wallet());
            System.out.println("Forwarding " + value.toFriendlyString());
            // Now send the coins back! Send with a small fee attached to ensure rapid confirmation.
            final Coin amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            final Wallet.SendResult sendResult = kit.wallet().sendCoins(kit.peerGroup(), forwardingAddress, amountToSend);
            checkNotNull(sendResult);  // We should never try to send more coins than we have!
            System.out.println("Sending ...");
            // Register a callback that is invoked when the transaction has propagated across the network.
            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
            // need access to the object the future returns.
            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public  void run() {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.out.println("Sent coins onwards! Transaction hash is " + sendResult.tx.getHashAsString());
                }
            }, MoreExecutors.directExecutor()); //sameThreadExecutor());
        } catch (KeyCrypterException | InsufficientMoneyException e) {
            // We don't use encrypted wallets in this example - can never happen.
            throw new RuntimeException(e);
        }
    }
}

