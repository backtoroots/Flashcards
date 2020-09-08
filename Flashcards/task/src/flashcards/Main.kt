package flashcards

import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.random.Random.Default.nextInt

enum class CommandLineEnum(var str: String) {
    IMPORT("-import"),
    EXPORT("-export")
}

class Logger(var log: MutableList<String> = mutableListOf()) {
    fun printLog(message: String) {
        println(message)
        log.add(message + "\n")
    }

    fun updateLog(message: String) = log.add(message)
}

class FlashcardsGame {
    private val scan = Scanner(System.`in`)
    private val flashcards: MutableList<Flashcard?> = mutableListOf()

    private val log = Logger()

    fun startGame(args: Array<String>) {
        if (args.contains(CommandLineEnum.IMPORT.str) &&
                args.size > 1 &&
                args[args.indexOf(CommandLineEnum.IMPORT.str) + 1] != CommandLineEnum.EXPORT.str)
            importFlashcardsWithFileName(fileName = args[args.indexOf(CommandLineEnum.IMPORT.str) + 1])
        loop@ while (true) {
            log.printLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            val command = scan.nextLine()
            log.updateLog(command + "\n")
            when (command) {
                "add" -> addFlashcard()
                "remove" -> removeFlashcard()
                "import" -> importFlashcards()
                "export" -> exportFlashcards()
                "ask" -> askFlashcards()
                "log" -> createLog()
                "hardest card" -> hardestCard()
                "reset stats" -> resetStats()
                "exit" -> {
                    log.printLog("Bye bye!")
                    if (args.contains(CommandLineEnum.EXPORT.str) &&
                            args.size > 1 &&
                            args[args.indexOf(CommandLineEnum.EXPORT.str) + 1] != CommandLineEnum.IMPORT.str)
                        exportFlashCardsWithFileName(fileName = args[args.indexOf(CommandLineEnum.EXPORT.str) + 1])
                    break@loop
                }
            }
            print("\n")
            log.updateLog("\n")
        }
    }

    private fun addFlashcard() {
        log.printLog("The card:")
        val term: String? = scan.nextLine()
        log.updateLog(term + "\n")
        if (term in flashcards.map { it?.term }) {
            log.printLog("The card \"${term}\" already exists.")
            return
        }
        log.printLog("The definition of the card:")
        val definition: String? = scan.nextLine()
        log.updateLog(definition + "\n")
        if (definition in flashcards.map { it?.definition }) {
            log.printLog("The definition \"${definition}\" already exists.")
            return
        }
        flashcards.add(Flashcard(term = term.toString(), definition = definition.toString()))
        log.printLog("The pair (\"${term}\":\"${definition}\") has been added.")
    }

    private fun removeFlashcard() {
        log.printLog("The card:")
        val term = scan.nextLine()
        log.updateLog("$term \n")
        if (flashcards.removeIf { it?.term == term })
            log.printLog("The card has been removed.")
        else
            log.printLog("Can't remove \"${term}\": there is no such card.")
    }

    private fun importFlashcardsWithFileName(fileName: String) {
        try {
            val text = File(fileName).readLines()
            for (line in text) {
                val (term, definition, mistakes) = line.trim().split(";")
                flashcards.removeIf { it?.term == term }
                flashcards.add(Flashcard(term = term, definition = definition, mistakes = mistakes.toInt()))
            }
            log.printLog("${text.size} cards have been loaded.")
        } catch (e: FileNotFoundException) {
            log.printLog("File not found.")
        }
    }

    private fun importFlashcards() {
        log.printLog("File name:")
        val fileName = scan.nextLine()
        log.updateLog(fileName + "\n")
        importFlashcardsWithFileName(fileName)
    }

    private fun exportFlashCardsWithFileName(fileName: String) {
        File(fileName).writeText("")
        for (i in flashcards.indices) File(fileName).appendText(
                "${flashcards[i]?.term};${flashcards[i]?.definition};${flashcards[i]?.mistakes}\n")
        log.printLog("${flashcards.size} cards have been saved.")
    }

    private fun exportFlashcards() {
        log.printLog("File name:")
        val fileName = scan.nextLine()
        log.updateLog(fileName + "\n")
        exportFlashCardsWithFileName(fileName)
    }

    private fun askFlashcards() {
        log.printLog("How many times to ask?")
        val numberOfFlashcards = scan.nextLine().toInt()
        log.updateLog(numberOfFlashcards.toString() + "\n")
        for (i in 0 until numberOfFlashcards) {
            val flashcard = flashcards[nextInt(flashcards.size)]
            log.printLog("Print the definition of \"${flashcard?.term}\":")
            val answer = scan.nextLine()
            log.updateLog(answer + "\n")
            if (answer == flashcard?.definition) {
                log.printLog("Correct!")
            } else {
                flashcard!!.mistakes += 1
                if (answer in flashcards.map { it?.definition })
                    log.printLog("Wrong. The right answer is \"${flashcard?.definition}\", but your definition is correct for \"${flashcards.filter { it?.definition == answer }[0]?.term}\"")
                else
                    log.printLog("Wrong. The right answer is \"${flashcard?.definition}\"")
            }
        }
    }

    private fun createLog() {
        log.printLog("File name:")
        val filename = scan.nextLine()
        val file = File(filename)
        log.updateLog(filename + "\n")
        file.writeText("")
        log.printLog("The log has been saved.")
        for (line in log.log) file.appendText(line)
    }

    private fun hardestCard() {
        val hardest = flashcards.filter {
            it?.mistakes!! > 0
                    && it.mistakes == flashcards.maxWith(
                    Comparator { card1, card2 -> card1!!.mistakes - card2!!.mistakes })?.mistakes
        }
        when (hardest.size) {
            0 -> log.printLog("There are no cards with errors.")
            1 -> log.printLog("The hardest card is \"${hardest[0]?.term}\". You have ${hardest[0]?.mistakes} errors answering it.")
            else -> {
                val cards = hardest.fold(
                        initial = "",
                        operation = { acc, next -> acc + "\"" + next?.term.toString() + "\", " }
                )
                log.printLog("The hardest cards are ${cards.subSequence(0, cards.length - 2)}. You have ${hardest[0]?.mistakes} errors answering them.")
            }
        }
    }

    private fun resetStats() {
        flashcards.forEach { it?.mistakes = 0 }
        log.printLog("Card statistics have been reset.")
    }
}

class Flashcard(val term: String, val definition: String, var mistakes: Int = 0) {
    override fun toString(): String = "$term $definition $mistakes"
}

fun main(args: Array<String>) = FlashcardsGame().startGame(args)
