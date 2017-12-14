package sample.domain;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;
import net.jini.core.lease.Lease;
import sample.SpaceUtils;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;

public class BaseModel {
    private JavaSpace05 space;
    public BaseModel() {
        space = (JavaSpace05) SpaceUtils.getSpace();
        if(space == null) {
            System.err.println("JavaSpace not found.");
            System.exit(1);
        }
    }

    public MatchSet readEntries(Entry template) throws RemoteException, TransactionException {
        Collection<Entry> templates = new ArrayList<>();
        templates.add(template);
        MatchSet results = null;
        try {
            results = space.contents(templates, null, 500, 100);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public void writeEntry(Entry template) throws RemoteException, TransactionException{
        try {
            space.write(template, null, Lease.FOREVER);
        } catch (TransactionException e) {
            e.printStackTrace();
        }
    }

    public Entry readEntry(Entry template){
        Entry result = null;
        try {
            result = space.readIfExists(template, null,500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public Entry takeEntry(Entry template) {
       Entry result = null;
        try {
            result = space.takeIfExists(template, null,Lease.FOREVER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void registerForAvailability(List<Entry> templates, RemoteEventListener listener) throws RemoteException, TransactionException {
        space.registerForAvailabilityEvent(templates, null, false, listener, Lease.FOREVER, null);
    }
}
