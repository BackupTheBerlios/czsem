#!/usr/bin/perl
use strict;
use warnings;

use Treex::Core;
use Treex::Block::CzsemRpcReader;
use Frontier::Daemon;
use Net::Address::IP::Local;
use Sys::Hostname;
use Treex::Core::Log;


sub debugPrint {
  #print STDERR @_;
}

debugPrint "treex online start\n";


Treex::Core::Log::log_set_error_level('INFO');


my $port_number = 9090;
my $isReady = 0;
my $scenario = undef;


sub startServer
{
    my $methods = {
      'treex.isReady' => \&isReady,
      'treex.analyzeDoc' => \&analyzeDoc,
      'treex.encodeDoc' => \&encodeDoc,
      'treex.terminate' => \&terminate };

    my $host = hostname;
    
    print "Starting Treex RPC server at http://$host:$port_number/RPC2 ( http://" . Net::Address::IP::Local->public . ":$port_number/RPC2 )\n";

    
    my $server;
    $server = Frontier::Daemon->new(LocalPort => $port_number, methods => $methods, use_objects => 0)
        or die "Couldn't start HTTP server: $!";
}


sub isReady
{
   return eval $isReady;
}

sub terminate
{
    exit;
}

sub initScenario
{
  $scenario = Treex::Core::Scenario->new(from_string => 'Util::SetGlobal language=en CzsemRpcReader W2A::EN::Segment devel\analysis\en\s_w2t.scen');
}



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
	
	$doc->save('C:\workspace\demo.treex');

}

sub analyzeDoc
{
  my $doc = Treex::Core::Document->new;
  
  my $zone = $doc->create_zone('en');
  $zone->set_text("Hallo world! Life is great, isn't it? John is the man who died in London recently."); 	
 
	
	$Treex::Block::CzsemRpcReader::dataQueue->enqueue($doc);  
  $Treex::Block::CzsemRpcReader::dataQueue->enqueue(undef);  
  $scenario->run;
  
  # DEBUG !!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	$doc->save('C:\workspace\demo.treex');
  # DEBUG !!!!!!!!!!!!!!!!!!!!!!!!!!!!
}


sub processNode
{
  my $node  = shift;
  my $ret = {};
  
  if ($node->parent) {
    debugPrint  "parent: ";
    debugPrint  $node->parent->id;
    $ret->{"parent_id"} = $node->parent->id;
  } 
  
  debugPrint  "\ntype: ";
  debugPrint  $node->get_pml_type_name . "\n";
  $ret->{"pml_type_name"} = $node->get_pml_type_name; 

  
  foreach my $path ( $node->attribute_paths ) {
    my $value = $node->attr($path);
    my $valueAll = $node->all($path);
    
    if ($value) {
      if (UNIVERSAL::isa($value,'ARRAY'))
      {
        my @arr_value = $value->values;
        debugPrint  "$path = @arr_value ($valueAll)\n";
        $ret->{$path} = @arr_value;           
      } else {
        debugPrint  "$path = $value ($valueAll)\n";
        $ret->{$path} = $value; 
      } 
    } 
  }  
  return $ret;
}

sub encodeDoc
{
  my $filename = shift;
  my $doc = Treex::Core::Document->new( { filename => $filename } ); 
  return encodeDocInMemory($doc); 
}

sub encodeDocInMemory
{
  my $doc = shift;
  
  my $zones = [];

  foreach my $bundle ( $doc->get_bundles ) {
    foreach my $bundlezone ( $bundle->get_all_zones ) {
      my $nodes = [];
      my $roots = [];
      my $sentence = $bundlezone->sentence; 
      my $language = $bundlezone->language; 
      my $selector = $bundlezone->selector; 

      debugPrint  "-----------sentence--------\n";      
      debugPrint  $bundlezone->sentence . "\n";      
      foreach my $root ( $bundlezone->get_all_trees ) {
        debugPrint  "--root--\n";
        debugPrint  $root->id;
        debugPrint  "\n";
        push(@$roots, processNode($root));          
        foreach my $node ( $root->get_descendants({}) ) {
          debugPrint  "----node------\n";
          
          push(@$nodes, processNode($node));          
        }
      }
      my $zone = {         
        language => $language, 
        selector => $selector, 
        roots    => $roots,
        nodes    => $nodes,
        sentence => $sentence };
      
      push (@$zones, $zone);
    }
  }
  	
  return $zones;
}
 
#initScenario;

#analyzeDoc;

startServer;

#encodeDoc('C:\workspace\demo.treex');

debugPrint  "treex online end\n";

exit;


