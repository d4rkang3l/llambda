package llambda

class NoSyntaxRuleException(message : String) extends SemanticException(message)
class AmbiguousSyntaxRuleException(message : String) extends SemanticException(message)

object ExpandMacro {
  sealed abstract class Rewrite
  case class SubstituteRewrite(scope : Scope, identifier : String, expansion : sst.ScopedDatum) extends Rewrite
  case class SpliceRewrite(scope : Scope, identifier : String, expansion : List[sst.ScopedDatum]) extends Rewrite

  case class Expandable(template : sst.ScopedDatum, rewrites : List[Rewrite])

  private def matchRule(literals : List[String], pattern : List[sst.ScopedDatum], operands : List[sst.ScopedDatum]) : Option[List[Rewrite]] = {
    (pattern, operands) match {
      case (sst.ScopedSymbol(_, "_") :: restPattern, _ :: restOperands) =>
        // They used a wildcard - ignore this
        matchRule(literals, restPattern, restOperands)

      case (sst.ScopedSymbol(_, patternIdent) :: restPattern, sst.ScopedSymbol(_, operandIdent) :: restOperands) if literals.contains(patternIdent) =>
        if (patternIdent != operandIdent) {
          // They misused a literal - no match
          None
        }

        // The literal doesn't cause any rewrites. We just ignore it
        matchRule(literals, restOperands, restOperands)

      case (sst.ScopedSymbol(patternScope, patternIdent) :: sst.ScopedSymbol(_, "...") :: Nil, operands) =>
        // This is a splice like the "rest ..." in (first rest ...)
        Some(List(SpliceRewrite(patternScope, patternIdent, operands)))

      case (sst.ScopedSymbol(patternScope, patternIdent) :: restPattern, operand :: restOperands) =>
        // If the match doesn't fail later add our rewrite rule on
        matchRule(literals, restPattern, restOperands) map { rewrites =>
          SubstituteRewrite(patternScope, patternIdent, operand) :: rewrites
        }

      case (Nil, Nil) =>
        // We both ended at the same time - this is expected
        Some(Nil)

      case _ =>
        None
    }
  }

  def expandTemplate(template : sst.ScopedDatum, rewrites : List[Rewrite]) : sst.ScopedDatum = {
    for(rewrite <- rewrites) {
      rewrite match {
        case SpliceRewrite(scope, identifier, expansion) =>
          template match {
            // TODO: Should we handle splices in the middle of the list?
            case sst.ScopedProperList(sst.ScopedSymbol(symScope, symIdentifier) :: sst.ScopedSymbol(_, "...") :: Nil) =>
              if ((symScope == scope) && (symIdentifier == identifier)) {
                return expansion.foldRight(sst.ScopedAtom(symScope, ast.EmptyList) : sst.ScopedDatum) { (car, cdr) =>
                  sst.ScopedPair(symScope, car, cdr) 
                }
              }

            case _ =>
              // Nothing
          }

        case SubstituteRewrite(scope, identifier, expansion) =>
          if (template == sst.ScopedSymbol(scope, identifier)) {
            // The expansion should be already fully expanded - we don't need to recurse
            return expansion
          }
      }
    }

    template match {
      case sst.ScopedPair(scope, car, cdr) =>
        sst.ScopedPair(scope, expandTemplate(car, rewrites), expandTemplate(cdr, rewrites))

      case _ =>
        template
    }
  }

  def apply(syntax : BoundSyntax, operands : List[sst.ScopedDatum]) : sst.ScopedDatum = {
    val possibleExpandables = syntax.rules.flatMap { rule =>
      matchRule(syntax.literals, rule.pattern, operands) map { rewrites =>
        Expandable(rule.template, rewrites)
      }
    }

    possibleExpandables match {
      case Nil => throw new NoSyntaxRuleException(syntax.toString)
      case Expandable(template, rewrites) :: Nil =>
        // Expand!
        expandTemplate(template, rewrites)

      case _ =>
        throw new AmbiguousSyntaxRuleException(syntax.toString)
    }
  }
}

