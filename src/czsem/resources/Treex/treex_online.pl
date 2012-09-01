#!/usr/bin/env perl

use strict;
use warnings;

use Treex::Core;
use Treex::Block::CzsemRpcReader;
use Frontier::Daemon;
use Net::Address::IP::Local;
use Sys::Hostname;



my $port_number = 9090;

sub start_server
{
    my $methods;
    $methods = {
      'treex.isReady' => \&isReady,
      'treex.analyzeDoc' => \&analyzeDoc,
      'treex.terminate' => \&terminate };

    my $host = hostname;
    
    print STDERR "Starting Treex RPC server at http://$host:$port_number/RPC2 ( http://" . Net::Address::IP::Local->public . ":$port_number/RPC2 )\n";

    
    my $server;
    $server = Frontier::Daemon->new(LocalPort => $port_number, methods => $methods)
        or die "Couldn't start HTTP server: $!";
}

my $isReady = undef;

sub isReady
{
   return eval $isReady;
}

sub terminate
{
    exit;
}

my $scenario = Treex::Core::Scenario->new(from_string => 'Util::SetGlobal language=en CzsemRpcReader W2A::Segment W2A::Tokenize');


sub backup {
  my $doc = Treex::Core::Document->new;
	my $bundle = $doc->create_bundle;
	my $zone   = $bundle->create_zone('en');
	my $atree  = $zone->create_atree;
	 
	my $predicate = $atree->create_child({form=>'loves'});
	 
	foreach my $argument (qw(John Mary)) {
  		my $child = $atree->create_child( { form=>$argument } );
  		$child->set_parent($predicate);
  	}
	
	$doc->save('demo.treex');

}

sub analyzeDoc
{
 	
  my $doc = Treex::Core::Document->new;
  
  my $zone = $doc->create_zone('en');
  $zone->set_text("Hallo world!"); 	
 
	
	$Treex::Block::CzsemRpcReader::dataQueue->enqueue($doc);  
  $Treex::Block::CzsemRpcReader::dataQueue->enqueue(undef);  
  $scenario->run;
  
  foreach my $bundle ( $doc->get_bundles ) {
    foreach my $bundlezone ( $bundle->get_all_zones ) {
      print STDERR "-----------sentence--------\n";      
      print STDERR $bundlezone->sentence . "\n";      
      foreach my $root ( $bundlezone->get_all_trees ) {
        print STDERR "--root--\n";
        print STDERR $root->id;
        print STDERR "\n";
        foreach my $node ( $root->get_descendants({}) ) {
          print STDERR "----node------\n";
          foreach my $path ( $node->attribute_paths ) {
            my $value = $node->attr($path);
            my $valueAll = $node->all($path);
            if ($value) { 
              print STDERR "$path = $value ($valueAll)\n";
            } 
          }
          print STDERR "parent: ";
          print STDERR $node->parent->id;
          print STDERR "\ntype: ";
          print STDERR $node->get_pml_type_name . "\n";
        }
      }
    }
  }
	
	return { from=>{surname=>"dedek"} };
}
 

 	
start_server; 

print STDERR "treex online end\n";


 
