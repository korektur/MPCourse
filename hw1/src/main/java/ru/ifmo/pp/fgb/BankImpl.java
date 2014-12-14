package ru.ifmo.pp.fgb;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 * <p>
 *
 * @author Ruslan Akhundov
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     *
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getAmount(int index) {
        long amount;
        accounts[index].lock.lock();
        amount = accounts[index].amount;
        accounts[index].lock.unlock();
        return amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (int i = accounts.length - 1; i >= 0; --i) {
            accounts[i].lock.lock();
            sum += accounts[i].amount;
            accounts[i].lock.unlock();
        }
        return sum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        long resAmount;
        account.lock.lock();
        try {
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            account.amount += amount;
            resAmount = account.amount;
        } finally {
            account.lock.unlock();
        }
        return resAmount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
        long resAmount;
        account.lock.lock();
        try {
            if (account.amount - amount < 0) {
                throw new IllegalStateException("Underflow");
            }
            account.amount -= amount;
            resAmount = account.amount;
        } finally {
            account.lock.unlock();
        }
        return resAmount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];
        Lock first = accounts[Math.min(fromIndex, toIndex)].lock;
        Lock second = accounts[Math.max(fromIndex, toIndex)].lock;
        first.lock();
        second.lock();
        try {
            if (amount > from.amount) {
                throw new IllegalStateException("Underflow");
            } else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
                throw new IllegalStateException("Overflow");
            }
            from.amount -= amount;
            to.amount += amount;
        } finally {
            second.unlock();
            first.unlock();
        }
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Amount of funds in this account.
         */
        long amount;
        Lock lock;

        Account() {
            lock = new ReentrantLock();
        }
    }
}
