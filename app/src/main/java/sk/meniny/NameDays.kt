package sk.meniny

import java.time.LocalDate
import java.time.MonthDay

/** Jeden deň v kalendári: mená, prípadne názov sviatku (dni bez mien). */
data class NameDay(val date: MonthDay, val names: String, val holiday: String? = null) {
    val label: String get() = names.ifEmpty { holiday.orEmpty() }
    val nameList: List<String> get() = if (names.isEmpty()) emptyList() else names.split(", ")
}

/** Slovenský kalendár mien. Index v zozname = deň v mesiaci - 1. */
object NameDays {

    private val holidays = mapOf(
        MonthDay.of(1, 1) to "Deň vzniku Slovenskej republiky",
        MonthDay.of(5, 1) to "Sviatok práce",
        MonthDay.of(11, 2) to "Pamiatka zosnulých",
        MonthDay.of(12, 25) to "Prvý sviatok vianočný",
    )

    private val months: List<List<String>> = listOf(
        // Január
        listOf(
            "", "Alexandra, Karina", "Daniela", "Drahoslav", "Andrea", "Antónia", "Bohuslava",
            "Severín", "Alexej", "Dáša", "Malvína", "Ernest", "Rastislav", "Radovan", "Dobroslav",
            "Kristína", "Nataša", "Bohdana", "Drahomíra, Mário", "Dalibor", "Vincent", "Zora",
            "Miloš", "Timotej", "Gejza", "Tamara", "Bohuš", "Alfonz", "Gašpar", "Ema", "Emil",
        ),
        // Február
        listOf(
            "Tatiana", "Erika, Erik", "Blažej", "Veronika", "Agáta", "Dorota", "Vanda", "Zoja",
            "Zdenko", "Gabriela", "Dezider", "Perla", "Arpád", "Valentín", "Pravoslav",
            "Ida, Liana", "Miloslava", "Jaromír", "Vlasta", "Lívia", "Eleonóra", "Etela",
            "Roman, Romana", "Matej", "Frederik, Frederika", "Viktor", "Alexander", "Zlatica",
            "Radomír",
        ),
        // Marec
        listOf(
            "Albín", "Anežka", "Bohumil, Bohumila", "Kazimír", "Fridrich", "Radoslav, Radoslava",
            "Tomáš", "Alan, Alana", "Františka", "Branislav, Bruno", "Angela, Angelika", "Gregor",
            "Vlastimil", "Matilda", "Svetlana", "Boleslav", "Ľubica", "Eduard", "Jozef",
            "Víťazoslav, Klaudius", "Blahoslav", "Beňadik", "Adrián", "Gabriel", "Marián",
            "Emanuel", "Alena", "Soňa", "Miroslav", "Vieroslava", "Benjamín",
        ),
        // Apríl
        listOf(
            "Hugo", "Zita", "Richard", "Izidor", "Miroslava", "Irena", "Zoltán", "Albert",
            "Milena", "Igor", "Július", "Estera", "Aleš", "Justína", "Fedor", "Dana, Danica",
            "Rudolf, Rudolfa", "Valér", "Jela", "Marcel", "Ervín", "Slavomír", "Vojtech", "Juraj",
            "Marek", "Jaroslava", "Jaroslav", "Jarmila", "Lea", "Anastázia",
        ),
        // Máj
        listOf(
            "", "Žigmund", "Galina, Timea", "Florián", "Lesana, Lesia", "Hermína", "Monika",
            "Ingrida", "Roland", "Viktória", "Blažena", "Pankrác", "Servác", "Bonifác",
            "Žofia, Sofia", "Svetozár", "Gizela, Aneta", "Viola", "Gertrúda", "Bernard", "Zina",
            "Júlia, Juliana", "Želmíra", "Ela", "Urban, Vivien", "Dušan", "Iveta", "Viliam",
            "Vilma", "Ferdinand", "Petrana, Petronela",
        ),
        // Jún
        listOf(
            "Žaneta", "Xénia, Oxana", "Karolína", "Lenka", "Laura", "Norbert", "Róbert, Roberta",
            "Medard", "Stanislava", "Margaréta, Gréta", "Dobroslava", "Zlatko", "Anton", "Vasil",
            "Vít", "Blanka, Bianka", "Adolf", "Vratislav", "Alfréd", "Valéria", "Alojz",
            "Paulína", "Sidónia", "Ján", "Olívia, Tadeáš", "Adriána", "Ladislav, Ladislava",
            "Beáta", "Peter, Pavol, Petra", "Melánia",
        ),
        // Júl
        listOf(
            "Diana", "Berta", "Miloslav", "Prokop", "Cyril, Metod", "Patrik, Patrícia", "Oliver",
            "Ivan", "Lujza", "Amália", "Milota", "Nina", "Margita", "Kamil", "Henrich",
            "Drahomír, Rút", "Bohuslav", "Kamila", "Dušana", "Iľja, Eliáš", "Daniel",
            "Magdaléna", "Oľga", "Vladimír", "Jakub, Timur", "Anna, Hana, Anita", "Božena",
            "Krištof", "Marta", "Libuša", "Ignác",
        ),
        // August
        listOf(
            "Božidara", "Gustáv", "Jerguš", "Dominik, Dominika", "Hortenzia", "Jozefína",
            "Štefánia", "Oskar", "Ľubomíra", "Vavrinec", "Zuzana", "Darina", "Ľubomír", "Mojmír",
            "Marcela", "Leonard", "Milica", "Elena, Helena", "Lýdia", "Anabela, Liliana", "Jana",
            "Tichomír", "Filip", "Bartolomej", "Ľudovít", "Samuel", "Silvia", "Augustín",
            "Nikola, Nikolaj", "Ružena", "Nora",
        ),
        // September
        listOf(
            "Drahoslava", "Linda, Rebeka", "Belo", "Rozália", "Regína", "Alica", "Marianna",
            "Miriama", "Martina", "Oleg", "Bystrík", "Mária, Marlena", "Ctibor", "Ľudomil",
            "Jolana", "Ľudmila", "Olympia", "Eugénia", "Konštantín", "Ľuboslav, Ľuboslava",
            "Matúš", "Móric", "Zdenka", "Ľuboš, Ľubor", "Vladislav, Vladislava", "Edita",
            "Cyprián", "Václav", "Michal, Michaela", "Jarolím",
        ),
        // Október
        listOf(
            "Arnold", "Levoslav", "Stela", "František", "Viera", "Natália", "Eliška", "Brigita",
            "Dionýz", "Slavomíra", "Valentína", "Maximilián", "Koloman", "Boris", "Terézia",
            "Vladimíra", "Hedviga", "Lukáš", "Kristián", "Vendelín", "Uršuľa", "Sergej",
            "Alojzia", "Kvetoslava", "Aurel", "Demeter", "Sabína", "Dobromila", "Klára",
            "Šimon, Simona", "Aurélia",
        ),
        // November
        listOf(
            "Denis, Denisa", "", "Hubert", "Karol", "Imrich", "Renáta", "René", "Bohumír",
            "Teodor", "Tibor", "Martin, Maroš", "Svätopluk", "Stanislav", "Irma", "Leopold",
            "Agnesa", "Klaudia", "Eugen", "Alžbeta", "Félix", "Elvíra", "Cecília", "Klement",
            "Emília", "Katarína", "Kornel", "Milan", "Henrieta", "Vratko", "Ondrej, Andrej",
        ),
        // December
        listOf(
            "Edmund", "Bibiána", "Oldřich", "Barbora, Barbara", "Oto", "Mikuláš", "Ambróz",
            "Marína", "Izabela", "Radúz", "Hilda", "Otília", "Lucia", "Branislava, Bronislava",
            "Ivica", "Albína", "Kornélia", "Sláva, Slávka", "Judita", "Dagmara", "Bohdan",
            "Adela", "Nadežda", "Adam, Eva", "", "Štefan", "Filoména", "Ivana, Ivona", "Milada",
            "Dávid", "Silvester",
        ),
    )

    /** Všetkých 366 dní (vrátane 29. februára). */
    val all: List<NameDay> = months.flatMapIndexed { m, days ->
        days.mapIndexed { d, names ->
            val date = MonthDay.of(m + 1, d + 1)
            NameDay(date, names, holidays[date])
        }
    }

    fun forDate(date: LocalDate): NameDay = all.first { it.date == MonthDay.from(date) }
}
