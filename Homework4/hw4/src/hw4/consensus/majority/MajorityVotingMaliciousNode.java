package hw4.consensus.majority;

import hw4.net.*;
import hw4.net.Message;
import hw4.net.Send;
import hw4.net.Node;
import hw4.util.*;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class MajorityVotingMaliciousNode extends Node
{
    private boolean IsInitValue;
    private Value DecisionValue;
    private boolean IsLeaderValue;
    private Map<Id, Node> PeerNet = new HashMap<Id, Node>();
    private Map<Id, Value> PeerDValue = new HashMap<Id, Value>();

    public MajorityVotingMaliciousNode()
    {

    }

    @Override
    public List<Send> send(int round)
    {
        List<Send> s = new ArrayList();
        if(getIsLeader())
        {
            if(getLeaderInitialValue() == null)
            {
                throw new RuntimeException("Error in leader decision");
            }

            Value fradulentTest = null;
            for(Value v : getValueSet())
            {
                if(!v.equals(getLeaderInitialValue()))
                {
                    fradulentTest = v;
                    break;
                }
            }

            if(!IsInitValue)
            {
                for(Id i : getPeerIds())
                {
                    if(PeerNet.containsKey(i))
                    {
                        s.add(new Send(i, new MajorityVotingPayload(getLeaderInitialValue())));
                    }

                    else
                    {
                        if(i.getNumber() % 2 == 0)
                        {
                            s.add(new Send(i, new MajorityVotingPayload(getLeaderInitialValue())));
                        }

                        else
                        {
                            s.add(new Send(i, new MajorityVotingPayload(fradulentTest)));
                        }
                    }
                }

                IsInitValue = true;
                return s;
            }
        }

        else
        {
            Value fradulentDecisionTest = null;
            for(Value v : getValueSet())
            {
                if(!v.equals(DecisionValue))
                {
                    fradulentDecisionTest = v;
                    break;
                }
            }

            if(DecisionValue != null)
            {
                if(!IsLeaderValue)
                {
                    for(Id i2 : getPeerIds())
                    {
                        if(!PeerNet.containsKey(i2))
                        {
                            if(i2.getNumber() % 2 == 0)
                            {
                                s.add(new Send(i2, new MajorityVotingPayload(DecisionValue)));
                            }

                            else
                            {
                                s.add(new Send(i2, new MajorityVotingPayload(fradulentDecisionTest)));
                            }
                            IsLeaderValue = true;
                        }
                    }
                }
            }
        }
        return s;
    }

    @Override
    public void receive(int round, List<Message> messages)
    {
        if(!getIsLeader())
        {
            for(Message m : messages)
            {
                MajorityVotingPayload mvpl = m.getSend().getPayload(MajorityVotingPayload.class);
                if(mvpl != null)
                {
                    if(m.getFrom().equals(getLeaderNodeId()))
                    {
                        if(DecisionValue == null)
                        {
                            DecisionValue = mvpl.getDecisionValue();
                            PeerDValue.put(m.getFrom(), mvpl.getDecisionValue());
                        }
                    }

                    else
                    {
                        if(!PeerDValue.containsKey(m.getFrom()))
                        {
                            PeerDValue.put(m.getFrom(), mvpl.getDecisionValue());
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

    public void addSybil(MajorityVotingMaliciousNode n)
    {
        PeerNet.put(n.getId(), n);
    }
}