package hw4.consensus.bft;

import java.util.*;

import hw4.net.Id;
import hw4.util.HashMapList;

public class EIGTree
{
    private int Nodes;
    private int Level;
    private int Malicious = Level - 1;

    private HashMapList<Id, UnauthBFTPayload> tree;

    public EIGTree(int numNodes, int numLevel)
    {
        this.Level = numLevel;
        this.Nodes = numNodes;
    }

    public int getNumNodes()
    {
        return Nodes;
    }

    public int getNumMalicious()
    {
        return Malicious;
    }

    public int getNumLevels()
    {
        return Level;
    }

    public HashMapList<Id, UnauthBFTPayload> buildTree()
    {
        HashMapList<Id, UnauthBFTPayload> targetTree = new HashMapList<Id, UnauthBFTPayload>();
        int tempLevel = 0;

        for(int i = 0; i < getNumNodes(); i++)
        {
            List<Id> list = new ArrayList<>();
            Id id = new Id(i);
            list.add(id);

            Trace t = new Trace(list);
            UnauthBFTPayload ubpl = new UnauthBFTPayload(t, null);
            targetTree.put(id, ubpl);

            UnauthBFTPayload temp = ubpl;
            tempLevel = tempLevel + 1;

            while(tempLevel < getNumLevels())
            {
                for(int m = 0; m < getNumNodes(); m++)
                {
                    Trace oldtrace = temp.getTrace();
                    if(!oldtrace.getTrace().contains(new Id(m)))
                    {
                        Trace newtrace = Trace.append(oldtrace, new Id(m));
                        UnauthBFTPayload pl = new UnauthBFTPayload(newtrace, null);
                        targetTree.put(id, pl);
                        temp = pl;
                    }
                }
                tempLevel++;
            }
        }
        this.tree = targetTree;
        return targetTree;
    }

    public HashMapList<Id, UnauthBFTPayload> getTree()
    {
        return this.tree;
    }
}