package com.ailong.disruptor;

import com.ailong.disruptor.util.Util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 序列组
 */
public final class SequenceGroup extends Sequence {

    private static final AtomicReferenceFieldUpdater<SequenceGroup, Sequence[]> SEQUENCE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SequenceGroup.class, Sequence[].class, "sequences");
    private volatile Sequence[] sequences = new Sequence[0];


    public SequenceGroup() {
        super(-1);
    }

    @Override
    public long get() {
        return Util.getMinimumSequence(sequences);
    }

    @Override
    public void set(final long value) {
        final Sequence[] sequences = this.sequences;
        for (Sequence sequence : sequences) {
            sequence.set(value);
        }
    }

    public void add(final Sequence sequence) {
        Sequence[] oldSequences;
        Sequence[] newSequences;
        do {
            // cas 扩容
            oldSequences = sequences;
            final int oldSize = oldSequences.length;
            newSequences = new Sequence[oldSize + 1];
            System.arraycopy(oldSequences, 0, newSequences, 0, oldSize);
            newSequences[oldSize] = sequence;
        } while (!SEQUENCE_UPDATER.compareAndSet(this, oldSequences, newSequences));
    }

    public boolean remove(final Sequence sequence) {
        return SequenceGroups.removeSequence(this, SEQUENCE_UPDATER, sequence);
    }

    public int size() {
        return sequences.length;
    }


    public void addWhileRunning(Cursored cursored, Sequence sequence) {
        SequenceGroups.addSequences(this, SEQUENCE_UPDATER, cursored, sequence);
    }

}
