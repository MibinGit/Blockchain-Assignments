package hw3;

import java.security.*;
import java.util.*;

//Scrooge creates coins by adding outputs to a transaction to his public key.
//In ScroogeCoin, Scrooge can create as many coins as he wants.
//No one else can create a coin.
//A user owns a coin if a coin is transfer to him from its current owner
public class DefaultScroogeCoinServer implements ScroogeCoinServer
{
	private KeyPair scroogeKeyPair;
	private ArrayList<Transaction> ledger = new ArrayList();

	//Set scrooge's key pair
	@Override
	public synchronized void init(KeyPair scrooge)
	{
//		throw new RuntimeException();
		try
		{
			PublicKey publicKey = scrooge.getPublic();
			PrivateKey privateKey = scrooge.getPrivate();
			scroogeKeyPair = new KeyPair(publicKey, privateKey);
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//For every 10 minute epoch, this method is called with an unordered list of proposed transactions
	// 		submitted during this epoch.
	//This method goes through the list, checking each transaction for correctness, and accepts as
	// 		many transactions as it can in a "best-effort" manner, but it does not necessarily return
	// 		the maximum number possible.
	//If the method does not accept an valid transaction, the user must try to submit the transaction
	// 		again during the next epoch.
	//Returns a list of hash pointers to transactions accepted for this epoch

	public synchronized List<HashPointer> epochHandler(List<Transaction> txs)
	{
//		throw new RuntimeException();
		List<HashPointer> list = new ArrayList<HashPointer>();
		while(!txs.isEmpty())
		{
			List<Transaction> trans = new ArrayList<Transaction>();
			for(Transaction ts : txs)
			{
				if(isValid(ts))
				{
					ledger.add(ts);
					HashPointer hashP = new HashPointer(ts.getHash(), ledger.size() - 1);
					list.add(hashP);
				}

				else
				{
					trans.add(ts);
				}
			}

			if(txs.size() == trans.size())
			{
				break;
			}

			txs = trans;
		}

		return list;
	}

	//Returns true if and only if transaction tx meets the following conditions:
	//CreateCoin transaction
	//	(1) no inputs
	//	(2) all outputs are given to Scrooge's public key
	//	(3) all of tx’s output values are positive
	//	(4) Scrooge's signature of the transaction is included

	//PayCoin transaction
	//	(1) all inputs claimed by tx are in the current unspent (i.e. in getUTOXs()),
	//	(2) the signatures on each input of tx are valid,
	//	(3) no UTXO is claimed multiple times by tx,
	//	(4) all of tx’s output values are positive, and
	//	(5) the sum of tx’s input values is equal to the sum of its output values;
	@Override
	public synchronized boolean isValid(Transaction tx)
	{
//		throw new RuntimeException();
		switch(tx.getType())
		{
			case Create:
				if(tx.numInputs() != 0)
				{
					return false;
				}

				for(Transaction.Output op : tx.getOutputs())
				{
					if(op.getPublicKey() != scroogeKeyPair.getPublic())
					{
						return false;
					}

					if(op.getValue() <= 0)
					{
						return false;
					}
				}

				try
				{
					Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
					sig.initVerify(scroogeKeyPair.getPublic());
					sig.update(tx.getRawBytes());

					if(!sig.verify(tx.getSignature()))
					{
						return false;
					}
				}

				catch(Exception e)
				{
					e.printStackTrace();
				}

				return true;

			case Pay:
				Set<UTXO> utxo = getUTXOs();
				double sumIn = 0;
				double sumOut = 0;

				for(int i = 0; i < tx.numInputs(); i++)
				{
					Transaction.Input ip = tx.getInputs().get(i);
					int ledgerindex = getLedgerIndex(utxo, ip);
					if(ledgerindex < 0)
					{
						return false;
					}

					Transaction.Output o = ledger.get(ledgerindex).getOutput(ip.getIndexOfTxOutput());
					sumIn = o.getValue() + sumIn;
					PublicKey pk = o.getPublicKey();

					try
					{
						Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
						sig.initVerify(pk);
						sig.update(tx.getRawDataToSign(i));

						if(!sig.verify(ip.getSignature()))
						{
							return false;
						}
					}

					catch(Exception e)
					{
						e.printStackTrace();
					}
				}

				for(Transaction.Output op : tx.getOutputs())
				{
					if(op.getValue() < 0)
					{
						return false;
					}
					sumOut = op.getValue() + sumOut;
				}

				if(Math.abs(sumIn - sumOut) < .000001)
				{
					return true;
				}

				else
				{
					return false;
				}
		}
		return false;
	}

	private int getLedgerIndex(Set<UTXO> seenUTXO, Transaction.Input input)
	{
		for(int i = 0; i < ledger.size(); i++)
		{
			if(Arrays.equals(ledger.get(i).getHash(), input.getHashOfOutputTx()))
			{
				HashPointer hp = new HashPointer(input.getHashOfOutputTx(), i);
				UTXO temputxo = new UTXO(hp, input.getIndexOfTxOutput());

				if(seenUTXO.contains(temputxo))
				{
					return i;
				}
			}
		}
		return -1;
	}

	//Returns the complete set of currently unspent transaction outputs on the ledger
	@Override
	public synchronized Set<UTXO> getUTXOs()
	{
//		throw new RuntimeException();
		Set<UTXO> UTXO = new HashSet<UTXO>();
		for(int i = 0; i < ledger.size(); i++)
		{
			Transaction ts = ledger.get(i);
			switch(ts.getType())
			{
				case Create:
					for(Transaction.Output create : ts.getOutputs())
					{
						HashPointer createhp = new HashPointer(ts.getHash(), i);
						UTXO createUTXO = new UTXO(createhp, ts.getIndex(create));
						UTXO.add(createUTXO);
					}
					break;

				case Pay:
					for(int j = 0; j < ts.numInputs(); j++)
					{
						Transaction.Input input = ts.getInputs().get(j);
						HashPointer inhp = new HashPointer(input.getHashOfOutputTx(), getLedgerIndex(UTXO, input));
						UTXO inUTXO = new UTXO(inhp, input.getIndexOfTxOutput());
						UTXO.remove(inUTXO);
					}

					for(Transaction.Output output : ts.getOutputs())
					{
						int index = ts.getIndex(output);
						HashPointer outhp = new HashPointer(ts.getHash(), i);
						UTXO outUTXO = new UTXO(outhp,index);
						UTXO.add(outUTXO);
					}
					break;
			}
		}
		return UTXO;
	}
}