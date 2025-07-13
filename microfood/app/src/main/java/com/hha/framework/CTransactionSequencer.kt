package com.hha.framework

class CMaxCounter {
    var maximum: Int = 0
    private val subLevel = mutableMapOf<Int, CMaxCounter>()

    fun updateMaximum(maximum: Int) {
        if (maximum > this.maximum) {
            this.maximum = maximum
        }
    }

    fun getOrCreate(sequence: Int): CMaxCounter {
        updateMaximum(sequence)
        return subLevel.getOrPut(sequence) { CMaxCounter() }
    }

    fun getNewSequence(): Int {
        maximum += 1
        return maximum
    }

    fun exchangeSequence(sequence1: Int, sequence2: Int) {
        val counter1 = subLevel[sequence1]
        val counter2 = subLevel[sequence2]

        if (counter1 != null && counter2 != null) {
            subLevel[sequence1] = counter2
            subLevel[sequence2] = counter1
        }
    }

    fun getNewSubSequence(sequence: Int): Int {
        return subLevel.getOrPut(sequence) { CMaxCounter() }.getNewSequence()
    }

    fun getNewSubSubSequence(sequence: Int, subSequence: Int): Int {
        return subLevel.getOrPut(sequence) { CMaxCounter() }
            .subLevel.getOrPut(subSequence) { CMaxCounter() }
            .getNewSequence()
    }
}

class CTransactionSequencer {
    private val sequenceList = CMaxCounter()

    fun getNewSequence(): Int = sequenceList.getNewSequence()

    fun getNewSubSequence(sequence: Int): Int = sequenceList.getNewSubSequence(sequence)

    fun getNewSubSubSequence(sequence: Int, subSequence: Int): Int =
        sequenceList.getNewSubSubSequence(sequence, subSequence)

    fun exchangeSequence(sequence1: Int, sequence2: Int) {
        sequenceList.exchangeSequence(sequence1, sequence2)
    }

    fun setSortedItemList(list: List<CItem>) {
        for (item in list) {
            val maxCounter = sequenceList.getOrCreate(item.sequence)
            if (item.subSequence != 0) {
                val subCounter = maxCounter.getOrCreate(item.subSequence)
                if (item.subSubSequence != 0) {
                    subCounter.updateMaximum(item.subSubSequence)
                }
            }
        }
    }
}
