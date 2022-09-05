# boundalyzer

I wrote `boundalyzer` as a GUI for creating and analyzing Black Box Specifications as described in the book
[Cleanroom Software Engineering: Technology and Process](https://www.amazon.com/Cleanroom-Software-Engineering-Technology-Process/dp/B000REU39C/ref=sr_1_1?crid=34RP6LQST41QO&keywords=cleanroom+software+engineering+technology+and+process&qid=1662346689&s=books&sprefix=cleanroom+software+engineering+technology+and+process%2Cstripbooks%2C124&sr=1-1).
This software represents my own interpretation of the Black Box concept described in that book.

Each Black Box specification is a model of the interactions between a software artifact and its environment. Each 
input the artifact receives from its environment is a "stimulus", and the artifact gives "responses" to the environment.
The specification itself is given in terms of a stimulus history. So, given a particular sequence of stimuli, the black
box defines the artifact's response.

`boundalyzer` enables a software engineer to check the completeness and consistency of their model. If there are any stimuli sequences
that do not have specified responses, it will tell you. Similarly, if the response is ambiguous for a particular stimuli sequence, it
will tell you that as well. 

What I find particularly interesting about this approach is that is is a completely external model of a software artifact, 
independent of any particular implementation strategy. It operates at a high level of abstraction while enabling a software engineer
to find specification problems very easily. In turn, because of its high level of abstraction, it is straightforward to verify that
the system behavior corresponds to this model.
