package com.moodpatcher.AntiGrief;

public class BlockConflict {
    public enum BlockConflictCase {
        OK, GUEST, NOT_GUEST, ERROR, NEUTRAL
    }

    boolean conflict;
    BlockData blockData;
    BlockConflictCase conflictCase;

    BlockConflict(BlockData blockDBData, boolean conflict, BlockConflictCase blockConflictCase) {
        this.conflict = conflict;
        this.blockData = blockDBData;
        this.conflictCase = blockConflictCase;
    }

    @Override
    public String toString() {
        return "BlockConflict{conflict=" + conflict + ", blockData='" + blockData + "', conflictCase: " + conflictCase + "'}";
    }
}
