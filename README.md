# Assignment-2

The first part of the assignment functionally simulates the guests entering the maze with the plan of eating the cupcake only ifthey have not already done so, and they do not refill the cupcake. The one guest chosen to be a counter counts himself once, then for every uneaten cupcake he finds when entering the maze, he counts another guest and orders another cupcake. The program randomly selects threads to enter the maze to simulate the minotaur's decision of who to bring into the maze.

The only thread synchronization used in this program is the synchronized methods ensuring that the cupcake is only modified by one thread at a time, so there are no misunderstandings between the counter and the other guests. Otherwise, the guests move at their own pace, and new guests are brought in until the minotaur calls it or the counter knows all guests have entered. Because the program uses minimal thread synchronization techniques, it should be mostly efficient.

The second part of the assignment uses queue as I believe the queue will be most effective. It allows those who have not seen the vase to line up, guaranteeing that they will see it, rather than taking random chances every time they try to enter the room.

The queue uses locks to ensure that items are enqueued and dequeued in the sequential manner that they should. The functions of the queue were splt slightly into separate steps so that the threads would not be holding the locks as long. This should help the threads move through the queue faster. 

Both parts of the assignment have varying runttimes as the choice of who enters the queue or maze is random. Though with 100 guests, the runtime on my machine for these programs typically is far under 1 second.
