# RuneCoach

http://runecoach.derpthemeus.com

RuneCoach is a website that is designed to help users make informed decisions when building rune pages by providing them with information about the average performance of individual runes on each champion, and explaining which types of champions each rune is most effective on.

The site calculates a score for each rune by analyzing its average performance across many ranked games, and determines which types of champions the rune is most effective on by finding patterns in performance between groups of similar champions.

Rune coach is an entry into the [2017 Riot Games API Challenge](https://discussion.developer.riotgames.com/articles/4395/the-riot-games-api-challenge-2017.html) by Derpthemeus and Skye3.

## Setup

The easiest way to use RuneCoach is to [check out the live version](http://runecoach.derpthemeus.com). If you want to run the server locally, follow the steps below. They're not extremely thorough, but they should help you get a local version running.

Prerequisites: [Git](https://git-scm.com/), [MySQL](https://www.mysql.com/), [JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

0. Clone the repo with `git clone https://github.com/Derpthemeus/RuneCoach.git` and run `cd RuneCoach`
0. Setup a MySQL server and give a user full access to a new database
0. Import the MySQL schema with `mysql DATABASE_NAME < schema.sql`
0. Import champion tags with `mysql DATABASE_NAME < tags.sql` (or create your own tags instead)
0. Set the environment variables `API_KEY` ([A Riot Games API key](https://developer.riotgames.com/)), `MYSQL_USERNAME` (the username of the MySQL account), `MYSQL_PASSWORD` (the password of the MySQL account), `MYSQL_CONNECTION_URL` (the JDBC URL of the MySQL server [e.g. `jdbc:mysql://localhost:3306/rune_coach`]), and `HTTP_PORT` (the port that the webserver should run on)
0. Start the server with `./gradlew clean runServer`
0. The webserver should now be live at the port specified earlier, and the database is being populated. The site won't do much until enough data has been aggregated.

## Development

I (Derpthemeus) did all of the backend stuff, and Skye3 created the UI and categorized champions. There were a few highlights in development, and a bunch of failures. Although RuneCoach did not turn out as well as I wanted, some parts that turned out very well, and I learned a lot from working on it.

I used this project as an opportunity to learn some technologies, and learned quite a bit about them. This project was my first time working with MySQL, Hibernate, and Jetty (which I only ended up getting my feet wet with). I also had minimal prior experience creating multithreaded applications (although the multithreading in this turned out to be quite simple), and had not done much work with databases before (I had played around with them a bit, but had never used more than the most basic queries, didn't have any experience working with indices, and had never done anything more complex than a single table with basic data).

### Database Population

The part of the project that I spent the most time working on was the database population (finding LoL matches to download, parsing data from the matches, and calculating aggregated stats). I was originally worried that it would be very difficult to setup due to my unfamiliarity with the technologies involved, but I think it turned out as the best part of the project. When I started working, I setup a few goals for the system:
* I wanted to separate each step of database population (finding summoners to track, finding match IDs to download, downloading matches, etc.) into different threads so I could adjust the number of instances of each type of thread based on the amount of work needed.
* I wanted to be able to run an arbitrary number of each type of thread, and be able to spawn or kill threads at any time without interfering with other running threads.
* I wanted to automatically detect how many of each thread I needed (based on API rate limits and the amount of work that each thread type needs to process), and automatically spawn and kill them accordingly.

My first approach to preventing conflicts between the threads was extremely naive. Having no idea how database locks worked, I assumed that I could lock each row that a thread was processing, and easily have the other threads ignore locked rows when looking for a row to process. I quickly learned that locks do not work like this, and solving this problem wouldn't be as simple as I was hoping. I consulted some more knowledgeable folks on the [Riot Games API community Discord server](https://discord.gg/riotapi), and received two suggestions for handling the issue.

The first suggestion was to have each thread query the database for a unique set of rows - if there were 3 threads running, the first thread would work on rows where `match_id % 3 == 0`, the second thread would work on rows where `match_id % 3 == 1`, and the third thread would work on rows where `match_id % 3 == 0`. However, I did not like this idea because it would not evenly balance work - it would be possible for match IDs to be distributed in a way such that 1 thread would have many rows to work on, but the other 2 threads would have no rows to work on.

I then got a second suggestion to query the database for a list of matches, split the list into a few sub-lists, then spawn a thread to process each sub-list. My first problem with this method was that each thread could finish processing its assigned matches at different times. Since I would need to wait for every thread to finish before spawning new ones, it wouldn't always operate at maximum efficiency.

These suggestions gave me some ideas, and I figured out a good approach to the issue. For each type of thread, I created a `PopulatorThread` subclass. `PopulatorThread` is a subclass of `Thread` that provides a way to gracefully kill running threads (by allowing them to finish the database row they are processing before killing them), and gives each instance a way to access its `PopulatorThreadSupervisor`. Each `PopulatorThread` subclass has a corresponding `PopulatorThreadSupervisor` subclass, which coordinates work between all instances of the populator thread. When a `PopulatorThread` needs a database row to operate on, it calls a method in its supervisor, which pops a row from a queue of rows to be processed, and returns it to the `PopulatorThread`. When the queue is empty, the supervisor refills it be querying the database. After querying the database, the supervisor checks which row each thread is currently processing, and removes that row from the queue so it won't be assigned to a second `PopulatorThread`.

Overall, I'm very happy with how everything turned out. It met the first 2 requirements I set, and is designed in a way that it could easily be updated to meet the 3rd requirement (unfortunately I did not have time to do this, and changing the number of threads to run currently requires modifying the code and recompiling the code). It does have some occasional errors related to locking and multithreading, but these just result in transactions being rolled back, and do not harm the integrity of the database. I have a theory as to what was causing this issue, but I did not have time to investigate it.


## Future Plans

The future of RuneCoach depends on its popularity. If it becomes popular and useful enough that continuing to work on it would be sensible and sustainable, there are a few things I want to improve:


* Possible rewrite the whole thing. A combination of poor planning and tight deadlines lead to some very poorly written code. It would be much easier to maintain if designed better.
* The tagging system. The current tagging system is a bit primitive: each tag is just a binary value (you can't specify exactly how tanky a champion is or how important their tankiness is to their identity), and each champion is treated as having a single playstyle (it doesn't matter if there's full tank Vayne build and a traditional ADC Vayne build - the program just treats them both as Vayne).
* Mis-tagging reporting. I want to automatically detect when a champion is performing differently than should be expected from a tag so it can be manually confirmed if they actually should have that tag.
* Database population errors. Right now, the database populators throw a fair amount of exceptions due to poorly setup threading and concurrency. Currently these are considered "acceptable" because they simply result in transactions being rolled back and have no serious effect on the integrity of the database. However, they should absolutely be fixed if this project is going to be maintained for a reasonable amount of time.
* Make the frontend code more maintainable. Right now the rune builder uses minified code copied from [the Runes Reforged announcement page](https://na.leagueoflegends.com/en/featured/preseason-update#builder), and is a huge pain to edit.


## Special Thanks

@stelar7 for creating [L4J8](https://github.com/stelar7/L4J8), which made my life much easier.

Members of the [Riot Games API community Discord server](https://discord.gg/riotapi) for helping me out with a few issues

The Riot Games API team for creating the Riot Games API and hosting the API challenge <3
