/*****************************************************************
 * This file is generated by gen-types.py. Do not edit manually. *
 *****************************************************************/

public:
	BoxedValue* car() const
	{
		return m_car;
	}

	BoxedValue* cdr() const
	{
		return m_cdr;
	}

private:
	BoxedValue* m_car;
	BoxedValue* m_cdr;