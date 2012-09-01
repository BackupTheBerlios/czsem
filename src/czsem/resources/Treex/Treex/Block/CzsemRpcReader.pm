package Treex::Block::CzsemRpcReader;
use Treex::Core;
use Treex::Core::Common;
use Moose;
use Thread::Queue;

extends 'Treex::Block::Read::BaseReader';

has '+from' => ( default => '' );


our $dataQueue = new Thread::Queue; 
    
sub next_document {
  my $dataElement = $dataQueue->dequeue;
  
  return $dataElement;
}

1;