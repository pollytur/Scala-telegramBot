package projectbot

import java.time.{LocalDate, Month}

import org.scalatest.{FlatSpec, Matchers}
import Parsing._

class ParsingSpec extends FlatSpec with Matchers {

  val idealGroups = List("B19-01", "B19-02", "B19-03", "B19-04", "B19-05", "B19-06",
    "B18-01", "B18-02", "B18-03", "B18-04", "B18-05", "B18-06",
    "B17-DS-01", "B17-DS-02", "B17-SE-01", "B17-SE-02", "B17-SB-01", "B17-RO-01",
    "B16-SE-01", "B16-RO-01", "B16-DS-01", "B16-DS-02",
    "M19-SE-01", "M19-SE-02", "M19-DS-01", "M19-DS-02", "M19-RO-01", "M18-RO-01")

  val idealElectives = List("Natural Language Processing and Machine Learning", "Computer Graphics in Game Development",
    "Advanced agile software design", "Modern Application Production", "Design Patterns", "Economics of Entrepreneurship in IT Industry",
    "Enterprise programming on Javascript - Advanced", "Total Virtualization", "Product's highly loaded architecture",
    "DevOps", "Human Computer Interaction Design for AI", "Technical Writing and Communication",
    "Introduction to  Public Speaking for IT-specialist", "Advanced Topics in Software Testing",
    "Critical Thinking for IT-specialist", "Reading Skills  for IT-specialist", "Introduction to IT Entrepreneurship",
    "Programming Windows Services with C++", "Advanced Academic Research- Writing and Performance",
    "Volunteer and Crowd-based Approaches in Computing", "Design Fiction", "Tech Startup Design", "Business-track",
    "Personal Efficiency Skills of IT-specialist", "Programming in Haskell",
    "Consensus theory and concurrent programming on a shared memory", "Functional Programming and Scala Language",
    "Practical Artificial Intelligence", "Introduction to Career Development for IT-specialist", "Mobile development using QT",
    "Psychology of IT-specialist", "Russian as a foreign language", "Software Requirements and Specifications")


  val m19labs = List(Labs("monday","10:35-12:05","advanced information retrieval","albina khusainova",313),
  Labs("tuesday","14:10-15:40","advanced machine learning","imad eddine ibrahim bekkouch",106),
  Labs("wednesday","12:10-13:40","advanced statistics","mohammad bahrami",321))
  
  val b1903labs = List(Labs("monday","10:35-12:05","english (5)","rabab marouf",101),
  Labs("tuesday","12:10-13:40","data structure and algorithms","dmitriy gordin",312),
  Labs("tuesday","14:10-15:40","analytical geometry and linear algebra 2","viktor kazorin",303),
  Labs("wednesday","14:10-15:40","introduction to programming 2","marat mingazov",318),
  Labs("thursday","14:10-15:40","english (5)","rabab marouf",101),
  Labs("thursday","15:45-17:15","theoretical computer science","mansur khazeev",318),
  Labs("friday","12:10-13:40","mathematical analysis 2","ramil dautov",318))



  val b19cores = List("monday", "14:10-15:40", "data structure and algorithms (lec) ", "adil khan", "108",
    "monday", "15:45-17:15", "data structure and algorithms (tutorial) ", "luiz araújo", "108",
    "tuesday", "09:00-10:30", "analytical geometry and linear algebra 2 (lec)", "yaroslav kholodov", "108",
    "tuesday", "10:35-12:05", "analytical geometry and linear algebra 2 (tutorial)", "ivan konyukhov", "108",
    "wednesday", "10:35-12:05", "introduction to programming 2 (lec)", "eugene zuev", "108",
    "wednesday", "12:10-13:40", "introduction to programming 2 (tutorial)", "ivan konyukhov", "108",
    "thursday", "09:00-10:30", "theoretical computer science (lec)", "manuel mazzara", "108",
    "thursday", "10:35-12:05", "theoretical computer science (tut.)", "daniel de carvalho", "108",
    "friday", "09:00-10:30", "mathematical analysis 2 (lec)", "sergey gorodetskiy", "108",
    "friday", "10:35-12:05", "mathematical analysis 2 (tutorial)", "sergey gorodetskiy", "108")

  val m19cores = List("tuesday", "10:35-12:05", "models of software systems (lec)", "manuel mazzara", "321",
    "thursday", "14:10-15:40", "architectures of software systems (lec)", "artem kruglov / mohamad kassab", "300",
    "friday", "14:10-15:40", "models of software systems (tutorial)", "alexander naumchev", "313")

  val b18All = List("MONDAY", "09:00-10:30", "Introduction to AI (Lec)", "Joseph Brown", "105",
    "MONDAY", "12:10-13:40", "Data Modeling and Databases 2 (Lec)", "Alexey Kanatov", "105",
    "MONDAY", "14:10-15:40", "Data Modeling and Databases 2 (Tutorial)", "Luiz Araújo", "105",
    "MONDAY", "15:45-17:15", "Introduction to AI (Lab)", "Hamna Aslam", "303",
    "TUESDAY", "09:00-10:30", "Control Theory (Lec)", "Sergey Savin", "105",
    "TUESDAY", "10:35-12:05", "Control Theory (Tutorial)", "Sergey Savin", "105",
    "TUESDAY", "15:45-17:15", "Control Theory (Lab)", "Shamil Mamedov", "316",
    "TUESDAY", "17:20-18:50", "Software Project (Prs.)*", "Alexandr Borisov", "318",
    "WEDNESDAY", "10:35-12:05", "Probability and Statistics (Lec)", "Sergey Gorodetskiy", "105",
    "WEDNESDAY", "12:10-13:40", "Probability and Statistics (tutorial)", "Sergey Gorodetskiy", "105",
    "WEDNESDAY", "14:10-15:40", "Data Modeling and Databases 2 (Lab)", "Hamza Salem", "316",
    "WEDNESDAY", "15:45-17:15", "Probability and Statistics (Lab)", "Pavel Khakimov", "320",
    "THURSDAY", "10:35-12:05", "Networks (Lec)", "Rasheed Hussain", "105",
    "THURSDAY", "12:10-13:40", "Networks (tutorial)", "Artem Burmyakov / Vadim Rashitov", "105",
    "THURSDAY", "14:10-15:40", "Networks (Lab)", "Vadim Rashitov", "317",
    "FRIDAY", "10:35-12:05", "Software Project (Lec)", "Evgenii Bobrov", "106",
    "FRIDAY", "12:10-13:40", "Software Project (Lab.)", "Ivan Dmitriev", "320")

  val allCources = (List(
    Core("monday", "14:10-15:40", "data structure and algorithms", "adil khan", 108, Some("monday"),
      Some("15:45-17:15"), Some("luiz araújo"), Some(108)),
    Core("tuesday", "09:00-10:30", "analytical geometry and linear algebra 2",
      "yaroslav kholodov", 108, Some("tuesday"), Some("10:35-12:05"), Some("ivan konyukhov"), Some(108)),
    Core("wednesday", "10:35-12:05",
      "introduction to programming 2", "eugene zuev", 108, Some("wednesday"), Some("12:10-13:40"), Some("ivan konyukhov"), Some(108)),
    Core("thursday", "09:00-10:30", "theoretical computer science", "manuel mazzara", 108, Some("thursday"), Some("10:35-12:05"),
      Some("daniel de carvalho"), Some(108)),
    Core("friday", "09:00-10:30", "mathematical analysis 2", "sergey gorodetskiy", 108, Some("friday"),
      Some("10:35-12:05"), Some("sergey gorodetskiy"), Some(108)),
    Core("monday", "09:00-10:30", "introduction to ai", "joseph brown",105,None, None, None, None),
    Core("monday", "12:10-13:40", "data modeling and databases 2", "alexey kanatov",105, Some("monday"), Some("14:10-15:40"),
      Some("luiz araújo"), Some(105)),
    Core("tuesday", "09:00-10:30", "control theory", "sergey savin",105, Some("tuesday"), Some("10:35-12:05"),
      Some("sergey savin"), Some(105)),
    Core("wednesday", "10:35-12:05", "probability and statistics", "sergey gorodetskiy",105, Some("wednesday"),
      Some("12:10-13:40"), Some("sergey gorodetskiy"), Some(105)),
    Core("thursday", "10:35-12:05", "networks", "rasheed hussain",105, Some("thursday"),
      Some("12:10-13:40"), Some("artem burmyakov / vadim rashitov"), Some(105)),
    Core("friday", "10:35-12:05", "software project", "evgenii bobrov", 106, None, None, None, None),
    Core("tuesday", "09:00-10:30", "digital signal processing", "nikolay shilov", 106, None, None, None, None),
    Core("tuesday", "10:35-12:05", "information retrieval", "alexey kanatov", 106, None, None, None, None),
    Core("wednesday", "12:10-13:40", "data mining", "leonid merkin", 106, None, None, None, None),
    Core("thursday", "10:35-12:05", "game theory", "joseph brown", 106, None, None, None, None),
    Core("tuesday", "14:10-15:40", "software systems design", "eugene zuev",105, None, None, None, None),
    Core("wednesday", "12:10-13:40", "lean software development", "giancarlo succi", 313, None, None, None, None),
    Core("tuesday", "09:00-10:30", "digital signal processing", "nikolay shilov", 106, None, None, None, None),
    Core("thursday", "10:35-12:05", "game theory", "joseph brown", 106, None, None, None, None),
    Core("monday", "14:10-15:40", "network and cyber security", "kirill saltanov/saif saad", 300, None, None, None, None),
    Core("tuesday", "10:35-12:05", "information retrieval", "a.kanatov", 106, None, None, None, None),
    Core("tuesday", "09:00-10:30", "digital signal processing", "nikolay shilov", 106, None, None, None, None),
    Core("thursday", "10:35-12:05", "game theory", "joseph brown", 106, None, None, None, None),
    Core("monday", "14:10-15:40", "robotics systems", "igor gaponov/alexander klimchik", 321, None, None, None, None),
    Core("wednesday", "12:10-13:40", "sensors and sensing", "ilya afanasyev", 317, None, None, None, None),
    Core("thursday", "10:35-12:05", "mechanics and machines", "alexandr maloletov", 318, None, None, None, None),
    Core("thursday", "14:10-15:40", "mechatronics", "igor gaponov", 306, None, None, None, None),
    Core("tuesday", "09:00-10:30", "digital signal processing", "nikolay shilov", 106, None, None, None, None),
    Core("tuesday", "09:00-10:30", "software quality and reliability", "andrey sadovykh", 313, None, None, None, None),
    Core("thursday", "14:10-15:40", "mobile development", "alexander simonenko", 313, None, None, None, None),
    Core("tuesday", "10:35-12:05", "practical machine learning and deep learning", "vladimir ivanov", 313, None, None, None, None),
    Core("tuesday", "14:10-15:40", "mobile robotics and autonomous driving", "gafurov salimzhan/ roman fedorenko", 321, None, None, None, None),
    Core("thursday", "14:10-15:40", "mechatronics", "igor gaponov", 306, None, None, None, None),
    Core("tuesday", "10:35-12:05", "practical machine learning and deep learning", "vladimir ivanov", 313, None, None, None, None),
    Core("tuesday", "10:35-12:05", "models of software systems", "manuel mazzara", 321, Some("friday"), Some("14:10-15:40"),
      Some("alexander naumchev"), Some(313)),
    Core("thursday", "14:10-15:40", "architectures of software systems", "artem kruglov / mohamad kassab", 300, None, None, None, None),
    Core("monday", "09:00-10:30", "advanced information retrieval", "stanislav protasov", 313, None, None, None, None),
    Core("tuesday", "12:10-13:40", "advanced machine learning", "muhammad fahim", 321, None, None, None, None),
    Core("wednesday", "10:35-12:05", "advanced statistics", "giancarlo succi", 321, None, None, None, None),
    Core("monday", "10:35-12:05", "computer vision", "muhammad fahim", 318, None, None, None, None),
    Core("monday", "14:10-15:40", "advanced robotics", "igor gaponov/alexander klimchik", 321, None, None, None, None),
    Core("tuesday", "14:10-15:40", "autonomous vehicles", "gafurov salimzhan/ roman fedorenko", 321, None, None, None, None),
    Core("monday", "10:35-12:05", "neurosciences", "alexander hramov", 320, None, None, None, None)),
    List((List(Labs("monday", "09:00-10:30", "english (1)", "rabab marouf", 101),
      Labs("tuesday", "12:10-13:40", "data structure and algorithms", "nikolay kudasov", 313),
      Labs("tuesday", "14:10-15:40", "analytical geometry and linear algebra 2", "ivan konyukhov", 320),
      Labs("wednesday", "14:10-15:40", "introduction to programming 2", "anastasia puzankova", 101),
      Labs("thursday", "12:10-13:40", "english (1)", "rabab marouf", 101),
      Labs("thursday", "14:10-15:40", "theoretical computer science", "swati megha", 314),
      Labs("friday", "12:10-13:40", "mathematical analysis 2", "alexey shikulin", 101)), Some("B19-01")),
      (List(Labs("monday", "09:00-10:30", "english (3)", "georgy gelvanovsky", 102),
        Labs("tuesday", "14:10-15:40", "data structure and algorithms", "nikolay kudasov", 316),
        Labs("tuesday", "15:45-17:15", "analytical geometry and linear algebra 2", "ivan konyukhov", 320),
        Labs("wednesday", "15:45-17:15", "introduction to programming 2", "anastasia puzankova", 101),
        Labs("thursday", "12:10-13:40", "english (3)", "georgy gelvanovsky", 102),
        Labs("thursday", "15:45-17:15", "theoretical computer science", "swati megha", 314),
        Labs("friday", "14:10-15:40", "mathematical analysis 2", "alexey shikulin", 101)), Some("B19-02")),
      (List(Labs("monday", "10:35-12:05", "english (5)", "rabab marouf", 101),
        Labs("tuesday", "12:10-13:40", "data structure and algorithms", "dmitriy gordin", 312),
        Labs("tuesday", "14:10-15:40", "analytical geometry and linear algebra 2", "viktor kazorin", 303),
        Labs("wednesday", "14:10-15:40", "introduction to programming 2", "marat mingazov", 318),
        Labs("thursday", "14:10-15:40", "english (5)", "rabab marouf", 101),
        Labs("thursday", "15:45-17:15", "theoretical computer science", "mansur khazeev", 318),
        Labs("friday", "12:10-13:40", "mathematical analysis 2", "ramil dautov", 318)), Some("B19-03")),
      (List(Labs("monday", "12:10-13:40", "english (7)", "rabab marouf", 101),
        Labs("tuesday", "14:10-15:40", "data structure and algorithms", "dmitriy gordin", 318),
        Labs("tuesday", "15:45-17:15", "analytical geometry and linear algebra 2", "viktor kazorin", 303),
        Labs("wednesday", "15:45-17:15", "introduction to programming 2", "marat mingazov", 318),
        Labs("thursday", "14:10-15:40", "theoretical computer science", "mansur khazeev", 318),
        Labs("thursday", "15:45-17:15", "english (7)", "rabab marouf", 101),
        Labs("friday", "14:10-15:40", "mathematical analysis 2", "ramil dautov", 318)), Some("B19-04")),
      (List(Labs("monday", "12:10-13:40", "english (9)", "georgy gelvanovsky", 102),
        Labs("tuesday", "12:10-13:40", "data structure and algorithms", "rasheed bader", 101),
        Labs("tuesday", "14:10-15:40", "analytical geometry and linear algebra 2", "anastasia puzankova", 101),
        Labs("wednesday", "14:10-15:40", "introduction to programming 2", "ivan konyukhov", 312),
        Labs("thursday", "14:10-15:40", "theoretical computer science", "naumcheva mariya", 312),
        Labs("thursday", "15:45-17:15", "english (9)", "georgy gelvanovsky", 102),
        Labs("friday", "12:10-13:40", "mathematical analysis 2", "evgeniy gryaznov", 316)), Some("B19-05")),
      (List(Labs("monday", "10:35-12:05", "english (11)", "georgy gelvanovsky", 102),
        Labs("tuesday", "14:10-15:40", "data structure and algorithms", "rasheed bader", 312),
        Labs("tuesday", "15:45-17:15", "analytical geometry and linear algebra 2", "anastasia puzankova", 101),
        Labs("wednesday", "15:45-17:15", "introduction to programming 2", "ivan konyukhov", 312),
        Labs("thursday", "14:10-15:40", "english (11)", "georgy gelvanovsky", 102),
        Labs("thursday", "15:45-17:15", "theoretical computer science", "naumcheva mariya", 312),
        Labs("friday", "14:10-15:40", "mathematical analysis 2", "evgeniy gryaznov", 316)), Some("B19-06")),
      (List(Labs("monday", "15:45-17:15", "introduction to ai", "hamna aslam", 303),
        Labs("tuesday", "15:45-17:15", "control theory", "shamil mamedov", 316),
        Labs("tuesday", "17:20-18:50", "software project", "alexandr borisov", 318),
        Labs("wednesday", "14:10-15:40", "data modeling and databases 2", "hamza salem", 316),
        Labs("wednesday", "15:45-17:15", "probability and statistics", "pavel khakimov", 320),
        Labs("thursday", "14:10-15:40", "networks", "vadim rashitov", 317),
        Labs("friday", "12:10-13:40", "software project", "ivan dmitriev", 320)), Some("B18-01")),
      (List(Labs("monday", "10:35-12:05", "introduction to ai", "hamna aslam", 303),
        Labs("tuesday", "14:10-15:40", "control theory", "mike ivanov", 306),
        Labs("tuesday", "15:45-17:15", "software project", "artem kruglov", 301),
        Labs("wednesday", "14:10-15:40", "probability and statistics", "pavel khakimov", 320),
        Labs("wednesday", "15:45-17:15", "data modeling and databases 2", "hamza salem", 316),
        Labs("thursday", "15:45-17:15", "networks", "vadim rashitov", 317),
        Labs("friday", "14:10-15:40", "software project", "artem kruglov", 303)), Some("B18-02")),
      (List(Labs("monday", "15:45-17:15", "introduction to ai", "rufina galieva", 314),
        Labs("tuesday", "15:45-17:15", "control theory", "mike ivanov", 306),
        Labs("tuesday", "17:20-18:50", "software project", "pavel kolychev", 321),
        Labs("wednesday", "14:10-15:40", "data modeling and databases 2", "anton tarasov", 314),
        Labs("wednesday", "15:45-17:15", "probability and statistics", "joseph lamptey", 306),
        Labs("thursday", "14:10-15:40", "networks", "marat mingazov", 301),
        Labs("friday", "14:10-15:40", "software project", "pavel kolychev", 317)), Some("B18-03")),
      (List(Labs("monday", "10:35-12:05", "introduction to ai", "rufina galieva", 317),
        Labs("tuesday", "14:10-15:40", "control theory", "shamil mamedov", 301),
        Labs("tuesday", "15:45-17:15", "software project", "alexandr borisov", 318),
        Labs("wednesday", "14:10-15:40", "probability and statistics", "joseph lamptey", 306),
        Labs("wednesday", "15:45-17:15", "data modeling and databases 2", "anton tarasov", 314),
        Labs("thursday", "15:45-17:15", "networks", "marat mingazov", 301),
        Labs("friday", "14:10-15:40", "software project", "ivan dmitriev", 320)), Some("B18-04")),
      (List(Labs("monday", "15:45-17:15", "introduction to ai", "nikita lozhnikov", 316),
        Labs("tuesday", "12:10-13:40", "software project", "artem kruglov", 301),
        Labs("tuesday", "15:45-17:15", "control theory", "oleg balakhnov", 317),
        Labs("wednesday", "14:10-15:40", "data modeling and databases 2", "nikita bogomazov", 303),
        Labs("wednesday", "15:45-17:15", "probability and statistics", "alexey shikulin", 317),
        Labs("thursday", "14:10-15:40", "networks", "pal sourabh", 320),
        Labs("friday", "15:45-17:15", "software project", "artem kruglov", 303)), Some("B18-05")),
      (List(Labs("monday", "17:20-18:50", "introduction to ai", "nikita lozhnikov", 316),
        Labs("tuesday", "14:10-15:40", "control theory", "oleg balakhnov", 317),
        Labs("tuesday", "15:45-17:15", "software project", "pavel kolychev", 321),
        Labs("wednesday", "14:10-15:40", "probability and statistics", "alexey shikulin", 317),
        Labs("wednesday", "15:45-17:15", "data modeling and databases 2", "nikita bogomazov", 303),
        Labs("thursday", "15:45-17:15", "networks", "pal sourabh", 320),
        Labs("friday", "15:45-17:15", "software project", "pavel kolychev", 317)), Some("B18-06")),
      (List(Labs("tuesday", "14:10-15:40", "information retrieval", "rustam gafarov", 313),
        Labs("tuesday", "15:45-17:15", "digital signal processing", "vitaliy romanov", 300),
        Labs("wednesday", "14:10-15:40", "data mining", "ivan grebenkin", 313),
        Labs("thursday", "12:10-13:40", "game theory", "munir makhmutov", 316),
        Labs("thursday", "17:20-18:50", "project (ds/ se/ r/ sbc) [if announced]", "rustam gafarov  / albina khusainova  " +
          "/ mansur khazeev / oleg bulichev / kirill saltanov", 105)), Some("B17-DS-01")),
      (List(Labs("tuesday", "14:10-15:40", "digital signal processing", "vitaliy romanov", 300),
        Labs("tuesday", "15:45-17:15", "information retrieval", "rustam gafarov", 313),
        Labs("wednesday", "15:45-17:15", "data mining", "ivan grebenkin", 313),
        Labs("thursday", "14:10-15:40", "game theory", "munir makhmutov", 316)), Some("B17-DS-02")),
      (List(Labs("tuesday", "10:35-12:05", "digital signal processing", "evgeniy gryaznov", 314),
        Labs("wednesday", "14:10-15:40", "software systems design", "sirojiddin komolov", 321),
        Labs("thursday", "12:10-13:40", "game theory", "manuel rodriguez", 312),
        Labs("thursday", "14:10-15:40", "lean software development", "ilya khomyakov", 303)), Some("B17-SE-01")),
      (List(Labs("tuesday", "12:10-13:40", "digital signal processing", "evgeniy gryaznov", 314),
        Labs("wednesday", "15:45-17:15", "software systems design", "sirojiddin komolov", 321),
        Labs("thursday", "12:10-13:40", "lean software development", "ilya khomyakov", 303),
        Labs("thursday", "14:10-15:40", "game theory", "manuel rodriguez", 321)), Some("B17-SE-02")),
      (List(Labs("monday", "15:45-17:15", "network and cyber security", "sergey grebennikov", 300),
        Labs("tuesday", "12:10-13:40", "information retrieval", "rustam gafarov", 303),
        Labs("tuesday", "15:45-17:15", "digital signal processing", "azat gaynutdinov", 314),
        Labs("thursday", "12:10-13:40", "game theory", "nikita bogomazov", 314)), Some("B17-SB-01")),
      (List(Labs("monday", "15:45-17:15", "robotics systems", "simeon nedelchev / mikhail ostanin", 317),
        Labs("tuesday", "14:10-15:40", "digital signal processing", "azat gaynutdinov", 102),
        Labs("wednesday", "14:10-15:40", "sensors and sensing", "geesara prathap kulathunga", 301),
        Labs("thursday", "12:10-13:40", "mechanics and machines", "oleg bulichev", 318),
        Labs("thursday", "15:45-17:15", "mechatronics", "stanislav mikhel", 306)), Some("B17-RO-01")),
      (List(Labs("tuesday", "10:35-12:05", "software quality and reliability", "shokhista ergasheva", 101),
        Labs("thursday", "15:45-17:15", "mobile development", "alexander simonenko", 313),
        Labs("thursday", "17:20-18:50", "philosophy", "farida nezhmetdinova", 106),
        Labs("thursday", "18:55-20:25", "philosophy", "farida nezhmetdinova", 106)), Some("B16-SE-01")),
      (List(Labs("tuesday", "12:10-13:40", "practical machine learning and deep learning", "youssef  ibrahim", 300),
        Labs("tuesday", "15:45-17:15", "mobile robotics and autonomous driving", "igor' shapovalov/ dmitry devitt", 312),
        Labs("thursday", "15:45-17:15", "mechatronics", "stanislav mikhel", 306)), Some("B16-RO-01")), (List(), Some("B16-DS-01")),
      (List(Labs("tuesday", "14:10-15:40", "practical machine learning and deep learning", "youssef  ibrahim", 314)), Some("B16-DS-02")),
      (List(Labs("tuesday", "12:10-13:40", "analysis of software artifacts", "andrey sadovykh", 106),
        Labs("thursday", "10:35-12:05", "analysis of software artifacts", "andrey sadovykh", 313),
        Labs("thursday", "15:45-17:15", "architectures of software systems (30.01; 06.02; 27.02; 09.04)", "mohamad kassab", 300),
        Labs("friday", "09:00-10:30", "architectures of software systems (31.01; 07.02; 28.02; 10.04)", "mohamad kassab", 313),
        Labs("friday", "10:35-12:05", "architectures of software systems (31.01; 07.02; 28.02; 10.04)", "mohamad kassab", 313)), Some("M19-SE-01")),
      (List(
        Labs("tuesday", "12:10-13:40", "analysis of software artifacts", "andrey sadovykh", 106),
        Labs("thursday", "10:35-12:05", "analysis of software artifacts", "andrey sadovykh", 313),
        Labs("thursday", "15:45-17:15", "architectures of software systems (30.01; 06.02; 27.02; 09.04)", "mohamad kassab", 300),
        Labs("friday", "09:00-10:30", "architectures of software systems (31.01; 07.02; 28.02; 10.04)", "mohamad kassab", 313),
        Labs("friday", "10:35-12:05", "architectures of software systems (31.01; 07.02; 28.02; 10.04)", "mohamad kassab", 313)),Some("M19-SE-02")),
      (List(Labs("monday", "10:35-12:05", "advanced information retrieval", "albina khusainova", 313),
        Labs("tuesday", "14:10-15:40", "advanced machine learning", "imad eddine ibrahim bekkouch", 106),
        Labs("wednesday", "12:10-13:40", "advanced statistics", "mohammad bahrami", 321)), Some("M19-DS-01")),
      (List(
        Labs("monday", "10:35-12:05", "advanced information retrieval", "albina khusainova", 313),
        Labs("tuesday", "14:10-15:40", "advanced machine learning", "imad eddine ibrahim bekkouch", 106),
        Labs("wednesday", "12:10-13:40", "advanced statistics", "mohammad bahrami", 321)), Some("M19-DS-02")),
      (List(Labs("monday", "12:10-13:40", "computer vision", "marcus freiherr ebner von eschenbach", 318),
        Labs("tuesday", "15:45-17:15", "autonomous vehicles", "igor' shapovalov /dmitry devitt", 312),
        Labs("wednesday", "10:35-12:05", "advanced robotics", "simeon nedelchev / mikhail ostanin", 318)), Some("M19-RO-01")),
      (List(Labs("monday", "12:10-13:40", "neurosciences", "andrey andreev", 320)), Some("M18-RO-01"))))


    val scala = List(Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.JANUARY, 20),
      "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.JANUARY, 23),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.JANUARY, 27),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.JANUARY, 30),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.FEBRUARY, 3),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.FEBRUARY, 6),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.FEBRUARY, 10),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.FEBRUARY, 13),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.FEBRUARY, 17),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.FEBRUARY, 20),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.FEBRUARY, 24),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.FEBRUARY, 27),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.MARCH, 2),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.MARCH, 5),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.MARCH, 9),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.MARCH, 12),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.MARCH, 16),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.MARCH, 19),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.MARCH, 23),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.MARCH, 26),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.MARCH, 30),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.APRIL, 2),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.APRIL, 6),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.APRIL, 9),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.APRIL, 13),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.APRIL, 16),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.APRIL, 20),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.APRIL, 23),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lab)", LocalDate.of(2020, Month.APRIL, 27),
        "Monday", "17:20-18:50", "Eugene Zuev", 101),
      Elective("Functional Programming and Scala Language (Lec)", LocalDate.of(2020, Month.APRIL, 30),
        "Thursday", "09:00-10:30", "Eugene Zuev", 101))
  
  val b18groupSubject = List("monday", "15:45-17:15", "introduction to ai (lab)", "rufina galieva", "314",
    "tuesday", "15:45-17:15", "control theory (lab)", "mike ivanov", "306",
    "tuesday", "17:20-18:50", "software project (prs.)*", "pavel kolychev", "321",
    "wednesday", "14:10-15:40", "data modeling and databases 2 (lab)", "anton tarasov", "314",
    "wednesday", "15:45-17:15", "probability and statistics (lab)", "joseph lamptey", "306",
    "thursday", "14:10-15:40", "networks (lab)", "marat mingazov", "301",
    "friday", "14:10-15:40", "software project (lab.)", "pavel kolychev", "317",
    "monday", "09:00-10:30", "introduction to ai (lec)", "joseph brown", "105",
    "monday", "12:10-13:40", "data modeling and databases 2 (lec)", "alexey kanatov", "105",
    "monday", "14:10-15:40", "data modeling and databases 2 (tutorial)", "luiz araújo", "105",
    "tuesday", "09:00-10:30", "control theory (lec)", "sergey savin", "105",
    "tuesday", "10:35-12:05", "control theory (tutorial)", "sergey savin", "105",
    "wednesday", "10:35-12:05", "probability and statistics (lec)", "sergey gorodetskiy", "105",
    "wednesday", "12:10-13:40", "probability and statistics (tutorial)", "sergey gorodetskiy", "105",
    "thursday", "10:35-12:05", "networks (lec)", "rasheed hussain", "105",
    "thursday", "12:10-13:40", "networks (tutorial)", "artem burmyakov / vadim rashitov", "105",
    "friday", "10:35-12:05", "software project (lec)", "evgenii bobrov", "106")

  val b16dsgroupd = List(Core("tuesday","10:35-12:05","practical machine learning and deep learning","vladimir ivanov",313,None,None,None,None))
  val m19se01Grouped = List(Core("tuesday","10:35-12:05","models of software systems","manuel mazzara",321,
    Some("friday"),Some("14:10-15:40"),Some("alexander naumchev"),Some(313)),
  Core("thursday","14:10-15:40","architectures of software systems","artem kruglov / mohamad kassab",300,None,None,None,None))


  val coreFile = "Core Courses_Spring_2019-2020 - BS,MS_Spring 2019.csv"
  val electiveFile = "Electives Schedule Spring 2020 Bachelors - Main.csv"


  "isWeekday monday" should "true" in {
    isWeekday("monday") shouldBe true
  }

  "isWeekday Tuesday" should "true" in {
    isWeekday("Tuesday") shouldBe true
  }

  "isWeekday wed" should "false" in {
    isWeekday("wed") shouldBe false
  }

  "isTime wed" should "false" in {
    isTime("wed") shouldBe false
  }

  "isTime 14:10" should "false" in {
    isTime("14:10") shouldBe false
  }

  "isTime 14:10-15:40" should "true" in {
    isTime("14:10-15:40") shouldBe true
  }

  "isTime 14:10-1n:40" should "false" in {
    isTime("14:10") shouldBe false
  }

  "isRoom 3123" should "false" in {
    isRoom("3123") shouldBe false
  }

  "isRoom 14:10" should "false" in {
    isRoom("14:10") shouldBe false
  }

  "isRoom 104" should "true" in {
    isRoom("104") shouldBe true
  }

  //there are examples in the doc with dots
  "isLecture mew (*)" should "true" in {
    isLecture("mew (*)") shouldBe true
  }

  "isLecture mew (lec)- small" should "true" in {
    isLecture("mew (lec)") shouldBe true
  }

  "isLecture mew (Lec) - big" should "true" in {
    isLecture("mew (Lec)") shouldBe true
  }

  //  spaces added
  "isLecture mew (Lec) with spaces" should "true" in {
    isLecture("mew (Lec)    ") shouldBe true
  }

  "isLecture mew (lab)" should "false" in {
    isLecture("mew (lab)") shouldBe false
  }

  "isLecture mew (tut)" should "false" in {
    isLecture("mew (tut)") shouldBe false
  }

  "isTutorial mew (tut.)-small" should "true" in {
    isTutorial("mew (tut.)") shouldBe true
  }

  "isTutorial mew (Tut.)-big" should "true" in {
    isTutorial("mew (Tut.)") shouldBe true
  }

  "isTutorial mew (tutorial)" should "true" in {
    isTutorial("mew (tutorial)") shouldBe true
  }

  "isTutorial mew (lab)" should "false" in {
    isTutorial("mew (lab)") shouldBe false
  }

  "isTutorial mew (lec)" should "false" in {
    isTutorial("mew (lec)") shouldBe false
  }

  "isLab mew(lec)" should "false" in {
    isLab("mew(lec)") shouldBe false
  }


  "isLab mew (tutorial)" should "false" in {
    isLab("mew (tutorial)") shouldBe false
  }

  "isLab Mew (Lab)" should "true" in {
    isLab("Mew (Lab)") shouldBe true
  }

  "isLab Mew(lab)" should "true" in {
    isLab("Mew(lab)") shouldBe true
  }

  "isLab Mew" should "true" in {
    isLab("Mew") shouldBe true
  }

  "groups" should "be ideal" in {
    groups(coreFile) shouldBe idealGroups
  }

  "listOfElectives" should "be ideal" in {
    listOfElectives(electiveFile) shouldBe idealElectives
  }

  "addLectures" should "not return labs" in {
    var checking = List.empty: List[Boolean]
    for (g <- Parsing.groups(coreFile)) {
      val res = Parsing.addLectures(coreFile, g)
      checking = checking :+ res.map(e => {
        !e.contains("(lab)") || !e.contains("(lab.)")
      }).foldLeft(true)(_ && _)
    }
    checking.foldLeft(true)(_ && _) shouldBe true
  }

  "addLectures b19-03" should "return b19cores" in {
    addLectures(coreFile, "B19-03") shouldBe b19cores
  }

  "addLectures M19-SE-02" should "return m19cores" in {
    addLectures(coreFile, "M19-SE-02") shouldBe m19cores
  }

  "groupLabs 13(B18-03)" should "return b18All" in {
    groupLabs(readData(coreFile), 13) shouldBe b18All
  }

  "groupLecturesAndTutorials" should "return m19se01Grouped" in {
    val intermediate = Parsing.addLectures(coreFile, "M19-SE-01")
    val customCourse = Parsing.groupLecturesAndTutorials(intermediate)
    customCourse shouldBe m19se01Grouped
  }

  "groupLecturesAndTutorials" should "return b16dsgroupd" in {
    val intermediate = Parsing.addLectures(coreFile, "B16-DS-01")
    val customCourse = Parsing.groupLecturesAndTutorials(intermediate)
    customCourse shouldBe b16dsgroupd
  }

  "groupSubjects" should "return b18groupSubject" in {
    groupSubjects(coreFile, "B18-03").fold(l => "left", r => r) shouldBe b18groupSubject
  }

  "groupSubjects" should "check if the group exists" in {
    groupSubjects(coreFile, "B20-03") shouldBe Left("there is no such group")
  }

  "toDate" should " should parse correctly" in {
    toDate("08/01/2020 ") shouldBe LocalDate.of(2020, Month.JANUARY, 8)

  }

  "fillingDatabase" should "work correctly" in {
    fillingDatabase(coreFile) shouldBe allCources
  }

  "onlyLabs b19-03" should "return b19labs" in {
    onlyLabs(coreFile,"B19-03") shouldBe b1903labs
  }

  "onlyLabs M19-DS-02" should "return b19labs" in {
    onlyLabs(coreFile,"M19-DS-02") shouldBe m19labs
  }

  "parseElectives scala" should "return scala" in {
    parseElectives("Functional Programming and Scala Language") shouldBe scala
  }


}
