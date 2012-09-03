#!/usr/bin/env perl

use strict;
use warnings;

use Treex::Core;
use Treex::Block::CzsemRpcReader;
use Frontier::Daemon;
use Net::Address::IP::Local;
use Sys::Hostname;
use Treex::Core::Log;
use Scalar::Util qw(reftype);


Treex::Core::Log::log_set_error_level('INFO');



my $port_number = 9090;
my $isReady = 0;
my $scenario = undef;

sub startServer
{
    my $methods = {
      'treex.isReady' => \&isReady,
      'treex.analyzeDoc' => \&analyzeDoc,
      'treex.terminate' => \&terminate };

    my $host = hostname;
    
    print STDERR "Starting Treex RPC server at http://$host:$port_number/RPC2 ( http://" . Net::Address::IP::Local->public . ":$port_number/RPC2 )\n";

    
    my $server;
    $server = Frontier::Daemon->new(LocalPort => $port_number, methods => $methods)
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


sub encodeDoc
{
  my $doc = shift;

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
            my $valueType = reftype($value);
            
            if (! $valueType) {$valueType=""}; 
            
            if ($value) { 
              print STDERR "$path = $value [$valueType] ($valueAll)\n";
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
 
#initScenario;

#analyzeDoc;

#startServer;

my $doc = Treex::Core::Document->new( { filename => 'C:\workspace\demo.treex' } ); 
encodeDoc($doc);

print STDERR "treex online end\n";

exit;


