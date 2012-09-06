package Treex::Block::CzsemRpcReader;
use Treex::Core;
use Treex::Core::Common;
use Moose;
use Thread::Queue;

extends 'Treex::Block::Read::BaseReader';

has '+from' => ( default => '' );

has language      => ( isa => 'Treex::Type::LangCode', is => 'ro', required => 1 );



our $dataQueue = new Thread::Queue; 
    
sub next_document {
  my ($self) = @_;
   
  my $docParams = $dataQueue->dequeue;
  
  return if !defined $docParams;
  
  my $doc = $docParams->{doc}; 
  my $text = $docParams->{text}; 

  my $zone = $doc->create_zone($self->language, $self->selector);
  $zone->set_text($text); 	

  
  return $doc;
}

1;