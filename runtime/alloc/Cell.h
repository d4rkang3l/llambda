#ifndef _LLIBY_ALLOC_CELL_H
#define _LLIBY_ALLOC_CELL_H

#include "binding/PairCell.h"

namespace lliby
{
namespace alloc
{

// This is a placeholder for size purposes
// We assume a PairCell is the largest allocation with two pointers
// If this isn't true then sizecheck.h will assert at compile time
class Cell : public PairCell
{
};

}
}

#endif
