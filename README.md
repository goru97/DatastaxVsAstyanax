# DatastaxVsAstyanax
Compare and Contrast Datastax java drivers for CQL with Astyanax thrift support for java. 

#Moving on from Astyanax to Datastax:

This is the description of the tests performed to monitor the performance of both Datastax Java-Driver and Astyanax Driver for Cassandra so as to make a decision regarding replacing the current Astyanax Driver used by Blueflood with Datastax Driver.

#Environment Setup:
•	Compute Machines:<br />

Four Rackspace’s On-metal Compute machines with the following configuration:<br />
CPU<br />
2.8 Ghz, 10 core Intel® Xeon® E5-2680 v2<br />
RAM<br />
32 GB<br />
System Disk<br />
32 GB<br />
Network<br />
Redundant 10 Gb / s connections in a high availability bond<br />
Each for Read using Astyanax, Read using Datastax, Write using Astyanax, Write using Datastax

•	Cassandra Cluster:

Cassandra (Version 2.0) cluster with four nodes was spun using Rackspace’s Cassandra Orchestration stack.
Each node with following configuration:<br />
CPU<br />
8 vCPUs<br />
RAM<br />
30 GB<br />
System Disk<br />
1.2 TB<br />
Network<br />
1.2 Gb / s<br />

•	Keyspace and Table:</br>

CREATE KEYSPACE "DATA" WITH replication = {</br>
  'class': 'SimpleStrategy',</br>
  'replication_factor': '3'</br>
};

use "DATA";</br>

CREATE TABLE metrics_full (</br>
  key text,</br>
  column1 bigint,</br>
  value blob,</br>
  PRIMARY KEY ((key), column1)</br>
) WITH COMPACT STORAGE AND</br>
  bloom_filter_fp_chance=0.010000 AND</br>
  caching='KEYS_ONLY' AND</br>
  comment='' AND</br>
  dclocal_read_repair_chance=0.100000 AND</br>
  gc_grace_seconds=864000 AND</br>
  index_interval=128 AND</br>
  read_repair_chance=0.000000 AND</br>
  replicate_on_write='true' AND</br>
  populate_io_cache_on_flush='false' AND</br>
  default_time_to_live=0 AND</br>
  speculative_retry='NONE' AND</br>
  memtable_flush_period_in_ms=0 AND</br>
  compaction={'class': 'SizeTieredCompactionStrategy'} AND</br>
  compression={'sstable_compression': 'LZ4Compressor'};</br>

•	Graphite:</br>

Graphite was used as a time-series database to store the metrics generated during the test.</br>

•	Grafana: To monitor the metrics using nice Graphs.</br>

#Running the program using maven:</br>
mvn clean install<br />
mvn exec:java -Dexec.args="'astyanax' 'read'" (For Read Operations using Astyanax Drivers).<br />
mvn exec:java -Dexec.args="'datastax' 'read'" (For Read Operations using Datastax Drivers).<br />
mvn exec:java -Dexec.args="'astyanax' 'write'" (For Write Operations using Astyanax Drivers).<br />
mvn exec:java -Dexec.args="'datastax' 'write'" (For Write Operations using Datastax Drivers).<br />
