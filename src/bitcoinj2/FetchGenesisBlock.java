/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitcoinj2;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.core.VersionMessage;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.wallet.Wallet;
/**
 *
 * @author user
 */
public class FetchGenesisBlock {
    public static void main(String[] args) throws Exception
    {
        //work with testnet
        final NetworkParameters netParamms = NetworkParameters.testNet();
        WalletAppKit wallet = new WalletAppKit(netParamms,new java.io.File("."),"hai" );
        wallet.startAsync();
        wallet.awaitRunning();
        System.out.println("done setup");
        BlockChain chain = wallet.chain();    
        
        BlockStore  blockStore = chain.getBlockStore();
        //ddeclare object to store and understand block chain
                
        try
        {
            //initialze blockchain object
             
            chain = new BlockChain(netParamms,blockStore);
            //instantiate peer to objct to handle connecions
            final PeerGroup peer = new PeerGroup(netParamms, chain);
            peer.setUserAgent("Sample App", Double.toString(1.0));
            //peer.addWallet(wallet);
            peer.addPeerDiscovery(new DnsDiscovery(netParamms));
            peer.start();
            peer.downloadBlockChain();
            
            //peer.waitForPeers(1).get();
            Peer peer1 = peer.getConnectedPeers().get(0);
            System.out.println("done connected");
            
            Sha256Hash blockHash = Sha256Hash.wrap("000000000000000f90768f606842977d8a0f0a516b310759d8c82fbc17228c13");
            
            Future<Block> future = peer1.getBlock(blockHash);
            //System.out.println(wallet);
            System.out.println("Waiting for node to send us he requested block : " +blockHash);
            Block block = future.get();
            
            //Block block = peer1.getBlock(blockStore.getChainHead().getHeader().getHash()).get();
            System.out.println("Here is the genesis block : \n" +block);
    
            //peer.stopAsync();
            
            //handle the various exception ; this need more work
            
            
        } catch (BlockStoreException e) {
            e.printStackTrace();
        } 
//        catch (UnknownHostException e){
//             e.printStackTrace();
//        } catch(IOException e){
//            e.printStackTrace();
//        } catch(InterruptedException e){
//            e.printStackTrace();
//        } catch(ExecutionException e){
//            e.printStackTrace();
//        }
    }
}
