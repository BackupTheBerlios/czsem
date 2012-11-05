#!/usr/bin/perl
use strict;
use warnings;

use Set::Light;

use Treex::Core;
use Treex::Block::CzsemRpcReader;
use Treex::CzsemScenario;
use RPC::XML::Server; 
use RPC::XML; 
use Net::Address::IP::Local;
use Sys::Hostname;
use Treex::Core::Log;

sub debugPrint {
#  print STDERR @_;
}

debugPrint "treex online start\n";


Treex::Core::Log::log_set_error_level('INFO');

our $stringAttrs; 
our $hashAttrs; 


my $port_number = shift(@ARGV);
my $handshake_hash = shift(@ARGV);

$port_number = 9090 unless defined $port_number;
$handshake_hash = '#default' unless defined $handshake_hash;




my $scenario = undef;

my @scenarioSetup = (
  'Util::SetGlobal language=cs',
  'CzsemRpcReader',
  'W2A::CS::Segment',
  'devel\analysis\cs\s_w2t.scen');


$RPC::XML::ENCODING = "UTF-8";


sub startServer
{
  my $srv = RPC::XML::Server->new(port => $port_number); #server object
  
  $srv->add_method(
   {
    "name"      => "treex.handshake", 
    "signature" => ['string'], 
    "code"      => \&handshake
   }
  );


  $srv->add_method(
   {
    "name"      => "treex.initScenario", 
    "signature" => ['string string array'], 
    "code"      => \&initScenarioSrv
   }
  );

  $srv->add_method(
   {
    "name"      => "treex.analyzeText", 
    "signature" => ['array string'], 
    "code"      => \&analyzeTextSrv
   }
  );

  $srv->add_method(
   {
    "name"      => "treex.analyzePreprocessedDoc", 
    "signature" => ['array string array'], 
    "code"      => \&analyzePreprocessedDocSrv
   }
  );

  $srv->add_method(
   {
    "name"      => "treex.encodeDoc", 
    "signature" => ['array string'], 
    "code"      => \&encodeDocSrv
   }
  );
  
  $srv->add_method(
   {
    "name"      => "treex.terminate", 
    "signature" => ['string'], #what are the return type and call parameters beware of the spaces!
    "code"      => \&terminate
   }
  );
  
  my $host = hostname;  
  print "Starting Treex RPC server at: \n  http://$host:$port_number \n  http://" . Net::Address::IP::Local->public . ":$port_number \n";
  print "Handshake hash: $handshake_hash\n";
  
  $srv->server_loop; # Just work
}


sub handshake
{
   return $handshake_hash;
}

sub terminate
{
    exit;
}

sub initScenario
{
  my $lang = shift;
  my $blocks = shift;
  my $scenStr = "Util::SetGlobal language=$lang CzsemRpcReader " . join(' ', @$blocks);  
  $scenario = Treex::CzsemScenario->new(from_string => $scenStr);
  
  $scenario->init;
  $scenario->start;
}


sub analyzeText
{
  return analyzePreprocessedDoc(shift, []);
}


sub processNode
{
  my $node  = shift;
  my $schema = shift;
  my $ret = {};
  
  if ($node->parent) {
    debugPrint  "parent: ";
    debugPrint  $node->parent->id;
    $ret->{"parent_id"} = $node->parent->id;
  } 
  
  debugPrint  "\ntype: ";
  debugPrint  $node->get_pml_type_name . "\n";
  $ret->{"pml_type_name"} = $node->get_pml_type_name; 



  #debugPrint $schema->get_type_names;
  my $nodeType = $schema->get_type_by_name($node->get_pml_type_name);
  debugPrint $nodeType . "\n";
  my $nodePaths = $schema->get_paths_to_atoms([$nodeType]);
  debugPrint  "nodePaths: $nodePaths\n";

  
#  foreach my $path ( $node->attribute_paths ) {
  foreach my $path ( $schema->get_paths_to_atoms([$nodeType]) ) {
   
   
    #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   #this is the most costly line !!!!!
    my $value = $node->attr($path);
    #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   
   
   
    #my $valueAll = $node->all($path);
    
    if ($value) {
      if (UNIVERSAL::isa($value,'ARRAY'))
      {
        my @arr_value = $value->values;
        my $finall_value = [];
        #debugPrint  "$path = @arr_value ($valueAll)\n";
        foreach my $v (@arr_value)
        {
          push(@$finall_value, $v);
        }
        
        $ret->{$path} = $finall_value;           
      } else {
        #debugPrint  "$path = $value ($valueAll)\n";
        utf8::encode($value); 
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
  return encodeLoadedDoc($doc); 
}

sub encodeLoadedDoc
{
  my $doc = shift;  
  my $zones = [];

  debugPrint  "-------------encodeLoadedDoc-$doc---------\n";      
 
  foreach my $bundle ( $doc->get_bundles ) {
    foreach my $bundlezone ( $bundle->get_all_zones ) {
      my $nodes = [];
      my $roots = [];
      my $sentence = $bundlezone->sentence;
      my $language = $bundlezone->language; 
      my $selector = $bundlezone->selector;
      
      utf8::encode($sentence); 
 
      debugPrint  "-----------sentence--------\n";      
      debugPrint  $bundlezone->sentence . "\n";      
      foreach my $root ( $bundlezone->get_all_trees ) {
        debugPrint  "--root--\n";
        debugPrint  $root->id;
        debugPrint  "\n";
        push(@$roots, processNode($root, $root->type->schema));          
        foreach my $node ( $root->get_descendants({}) ) {
          debugPrint  "----node------\n";
          
          push(@$nodes, processNode($node, $root->type->schema));          
        }
      }
      my $zone = {         
        language    => $language, 
        selector    => $selector, 
        roots       => $roots,
        nodes       => $nodes,
        sentence    => $sentence };
      
      push (@$zones, $zone);
    }
  }
  	
  return $zones;
}

sub debugPrintPreprocessedDoc 
{
  my $zones = shift;

  foreach my $zone ( @$zones ) {
    debugPrint  "--- ZONE ---\n";
    debugPrint  $zone->{'sentence'};
    debugPrint  "\n";
    
    my $tocs = $zone->{'tokens'};  
    foreach my $toc ( @$tocs ) {
      debugPrint $toc->{'lemma'};    
      debugPrint " ";    
    }
    debugPrint  "\n";
  }
}

sub analyzePreprocessedDoc
{
  my $text = shift;
  my $zones = shift;
  
  debugPrintPreprocessedDoc($zones);
  
  
  my $doc = Treex::Core::Document->new;
  
  my $docParams = {
     doc => $doc,
     text => $text,
     zones => $zones 
  };
 	
	$Treex::Block::CzsemRpcReader::dataQueue->enqueue($docParams);  
  $Treex::Block::CzsemRpcReader::dataQueue->enqueue(undef);  
  $scenario->run;
  
  # DEBUG !!!!!!!!!!!!!!!!!!!!!!!!!!!!
  #$doc->save('demo_cz.treex');
  # DEBUG !!!!!!!!!!!!!!!!!!!!!!!!!!!!
  
  return encodeLoadedDoc($doc);
}


sub analyzeTextSrv
{
  shift; #server context    
  return analyzeText(@_); 
}

sub encodeDocSrv
{
  shift; #server context
  return encodeDoc(@_);
}

sub initScenarioSrv
{
  shift; #server context
  return initScenario(@_);
}

sub analyzePreprocessedDocSrv
{
  shift; #server context    
  return analyzePreprocessedDoc(@_); 
}

sub printPathsForType
{
  my $schema = shift;
  my $typename = shift;
  
  my $nodeType = $schema->get_type_by_name($typename);
  
    print "------ $typename ------\n";

  
  foreach my $path ( $schema->get_paths_to_atoms([$nodeType]) ) {
    print "$path\n";
  }


}

sub printPaths
{
 my $doc = Treex::Core::Document->new;
 
 my $bundle = $doc->create_bundle;
 my $zone   = $bundle->create_zone('en');
 my $atree  = $zone->create_atree;
 
 my $schema = $atree->type->schema;
 
 foreach my $typeName ( $schema->get_type_names ) {
  print "$typeName\n";
 
 }
 
  printPathsForType($schema, 'a-node.type');
  printPathsForType($schema, 't-node.type');
  printPathsForType($schema, 'n-node.type');
  printPathsForType($schema, 'p-terminal.type');
  printPathsForType($schema, 'p-nonterminal.type');
  
   my $predicate = $atree->create_child({form=>'loves'});
 
 foreach my $argument (qw(John Mary)) {
   my $child = $atree->create_child( { form=>$argument } );
   $child->set_parent($predicate);
 }
 
 print "\n---\n";
 print $predicate->get_attr('form');
 print "\n---\n";
 print $predicate->{'form'};
 print "\n---\n";
 
 foreach my $attr (keys %{$predicate}) {
  print "attr $attr = xxx$predicate->{$attr}xxx\n"; 
  }


sub testDoc {

  my $document = Treex::Core::Document->new( { filename => 'C:\workspace\czsem_git\src\czsem\treex-gate-plugin\src\test\resources\czsem\gate\treex\demo_en.treex' } );
  
  
  foreach my $bundle ($document->get_bundles())
  {
    foreach my $root ($bundle->get_all_trees())
    {
      foreach my $node ($root->get_descendants({}))
      {
        print "------\n";
         foreach my $attr (keys %{$node}) {
          print "attr $attr = xxx$node->{$attr}xxx\n"; 
          }
      
      }
    
    }
  }
}


sub buildAttrs
{

  $stringAttrs = Set::Light->new; 
  $hashAttrs = Set::Light->new; 
  
  my $doc = Treex::Core::Document->new;
  my $bundle = $doc->create_bundle;
  my $schema = $bundle->type->schema;
  
  foreach my $typeName ( $schema->get_type_names ) {
    print "$typeName\n";
    
    my $nodeType = $schema->get_type_by_name($typeName);

    foreach my $path ( $schema->get_paths_to_atoms([$nodeType]) ) {
      print "   $path\n";
      
      my @parts = split(/\//, $path);
      
      $stringAttrs->insert(pop @parts);
      
      $hashAttrs->insert(@parts);
      
      print "   @parts\n";
    }
  }
  
  print "-str-\n";
  foreach my $elem (keys %{$stringAttrs} ) {
    print "$elem\n";
  }

  print "-hash-\n";
  foreach my $elem (keys %{ $hashAttrs} ) {
    print "$elem\n";
  }


}  

 
  
  #my $nodeType = $schema->get_type_by_name($node->get_pml_type_name);

 
  #my $nodePaths = $schema->get_paths_to_atoms([$atree->type]);
  #print  "nodePaths: $nodePaths\n";
  
   # foreach my $path ( $schema->get_paths_to_atoms([$nodeType]) ) {

}

 
#initScenario(@scenarioSetup);

#startServer;
printPaths;
testDoc;
buildAttrs;


