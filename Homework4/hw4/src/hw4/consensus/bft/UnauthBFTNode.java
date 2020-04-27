package hw4.consensus.bft;

import hw4.net.*;
import hw4.net.Message;
import hw4.net.Send;
import hw4.net.Node;
import hw4.util.*;

import java.util.*;

public class UnauthBFTNode extends Node
{
    private Value DecisionValue;
    private boolean IsInitValue;
    private boolean IsLeaderValue;
    private Map<Id, Value> PeerDValue = new HashMap<Id, Value>();
    private HashMapList<Id, UnauthBFTPayload> EIGTree;

    public UnauthBFTNode()
    {

    }

    public HashMapList<Id, UnauthBFTPayload> getEIG()
    {
        return EIGTree;
    }

    public void buildEIG()
    {
        int l = this.getRounds();
        int nSize = this.getPeerIds().size() + 1;

        EIGTree tree = new EIGTree(nSize, l);
        this.EIGTree = tree.getTree();
    }

    @Override
    public List<Send> send(int round)
    {
        //look the level ROUND-1 in the tree
        //for each vertex V in that level, create a message that sends the label
        //    of V to every other node in the network
        buildEIG();
        List<Send> s = new ArrayList<Send>();

        if(getIsLeader())
        {
            if(getLeaderInitialValue() == null)
            {
                throw new RuntimeException("Error in leader decision");
            }

            if(!IsInitValue)
            {
                for(Id i : getPeerIds())
                {
                    Id i2 = this.getId();
                    List<Id> list = new ArrayList<Id>();
                    list.add(i2);
                    list.add(i);
                    Trace t = new Trace(list);

                    UnauthBFTPayload ubpl = new UnauthBFTPayload(t, this.getLeaderInitialValue());
                    s.add(new Send(i, ubpl));
                }

                IsInitValue = true;
            }
        }

        else
        {
            if(DecisionValue != null)
            {
                if(!IsLeaderValue)
                {
                    for(Id i : getPeerIds())
                    {
                        Id i2 = this.getId();
                        List<Id> list = new ArrayList<Id>();
                        list.add(i2);
                        list.add(i);
                        Trace t = new Trace(list);

                        UnauthBFTPayload ubpl = new UnauthBFTPayload(t, DecisionValue);
                        s.add(new Send(i, ubpl));
                    }

                    IsLeaderValue = true;
                }
            }
        }
        return s;
    }

    @Override
    public void receive(int round, List<Message> messages)
    {
        //for each message, find the vertex V in level ROUND of tree,
        // and set the label of vertex indicated in the message
        if(!getIsLeader())
        {
            for(Message m : messages)
            {
                UnauthBFTPayload ubpl = m.getSend().getPayload(UnauthBFTPayload.class);
                if(ubpl != null)
                {
                    if(m.getFrom().equals(getLeaderNodeId()))
                    {
                        if(DecisionValue == null)
                        {
                            DecisionValue = ubpl.getDecisionValue();
                            PeerDValue.put(m.getFrom(), ubpl.getDecisionValue());
                        }
                    }

                    else
                    {
                        if(!PeerDValue.containsKey(m.getFrom()))
                        {
                            PeerDValue.put(m.getFrom(), ubpl.getDecisionValue());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void commit()
    {
        if(getIsLeader())
        {
            setDecisionValue(getLeaderInitialValue());
        }

        else
        {
            int mSize = getPeerIds().size() / 2 + 1;
            HashMapList<Value, Id> voteMap = new HashMapList<Value, Id>();
            for(Id i3 : getPeerIds())
            {
                Value tempValue = PeerDValue.get(i3);
                if(tempValue == null)
                {
                    tempValue = getDefaultValue();
                }
                voteMap.put(tempValue, i3);
            }

            System.out.println("Node " + getId() + "-> FromLeader: " + DecisionValue + "; PeerVotes: " + PeerDValue);

            boolean mFlag = false;
            for(Value v : voteMap.keySet())
            {
                if(voteMap.get(v).size() >= mSize)
                {
                    setDecisionValue(v);
                    mFlag = true;
                    break;
                }
            }

            if(!mFlag)
            {
                System.out.println("\tNo Majority.");
                setDecisionValue(getDefaultValue());
            }
        }
    }
}