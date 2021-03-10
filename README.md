# RTPE
This is a Spring Web application that searches for complex terms given a set of technical assets. The basic design is in this document - https://docs.google.com/document/d/1kjcAJoShgpb0v2MLrQbm_Yy3eBXfQk60ka0MIiXCQcE
The Controller gets a list of Technical terms. It gets relatedbusinessassetid fields out of the Elastic Repository. For each of these relatedbusinessassetif fields, it gets the relevant synonyms. 
For all of these business terms, it gets all possible combinations of all these business terms, synonyms and all.
For all combinations, it gets a Murmur3 hash of the business term Asset ID integers
It searches in the ES repository for the hash against the businessassethash and identifies the complex terms

# RTR-Asset-Generator
The id s of all business terms composing a complex term are used as input to create a business asset hash.
