export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[]

export type Database = {
  // Allows to automatically instantiate createClient with right options
  // instead of createClient<Database, { PostgrestVersion: 'XX' }>(URL, KEY)
  __InternalSupabase: {
    PostgrestVersion: "13.0.4"
  }
  public: {
    Tables: {
      applications: {
        Row: {
          applicant_id: string
          applied_at: string | null
          id: string
          message: string | null
          opportunity_id: string
          status: string | null
        }
        Insert: {
          applicant_id: string
          applied_at?: string | null
          id?: string
          message?: string | null
          opportunity_id: string
          status?: string | null
        }
        Update: {
          applicant_id?: string
          applied_at?: string | null
          id?: string
          message?: string | null
          opportunity_id?: string
          status?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "applications_opportunity_id_fkey"
            columns: ["opportunity_id"]
            isOneToOne: false
            referencedRelation: "opportunities"
            referencedColumns: ["id"]
          },
        ]
      }
      business_progress: {
        Row: {
          completed_at: string | null
          completion_percentage: number | null
          created_at: string | null
          id: string
          progress_data: Json | null
          tool_type: string
          updated_at: string | null
          user_id: string
        }
        Insert: {
          completed_at?: string | null
          completion_percentage?: number | null
          created_at?: string | null
          id?: string
          progress_data?: Json | null
          tool_type: string
          updated_at?: string | null
          user_id: string
        }
        Update: {
          completed_at?: string | null
          completion_percentage?: number | null
          created_at?: string | null
          id?: string
          progress_data?: Json | null
          tool_type?: string
          updated_at?: string | null
          user_id?: string
        }
        Relationships: []
      }
      impact_metrics: {
        Row: {
          created_at: string | null
          filters: Json | null
          id: string
          metric_type: string
          metric_value: number
          recorded_date: string | null
          region: string | null
        }
        Insert: {
          created_at?: string | null
          filters?: Json | null
          id?: string
          metric_type: string
          metric_value: number
          recorded_date?: string | null
          region?: string | null
        }
        Update: {
          created_at?: string | null
          filters?: Json | null
          id?: string
          metric_type?: string
          metric_value?: number
          recorded_date?: string | null
          region?: string | null
        }
        Relationships: []
      }
      mentorship_sessions: {
        Row: {
          answer: string | null
          answer_audio_url: string | null
          answered_at: string | null
          category: string | null
          created_at: string | null
          id: string
          is_public: boolean | null
          mentee_id: string
          mentor_id: string | null
          question: string
          question_audio_url: string | null
          status: string | null
        }
        Insert: {
          answer?: string | null
          answer_audio_url?: string | null
          answered_at?: string | null
          category?: string | null
          created_at?: string | null
          id?: string
          is_public?: boolean | null
          mentee_id: string
          mentor_id?: string | null
          question: string
          question_audio_url?: string | null
          status?: string | null
        }
        Update: {
          answer?: string | null
          answer_audio_url?: string | null
          answered_at?: string | null
          category?: string | null
          created_at?: string | null
          id?: string
          is_public?: boolean | null
          mentee_id?: string
          mentor_id?: string | null
          question?: string
          question_audio_url?: string | null
          status?: string | null
        }
        Relationships: []
      }
      opportunities: {
        Row: {
          category: string | null
          contact_info: string | null
          contact_method: string | null
          created_at: string | null
          deadline: string | null
          description: string
          employer_id: string
          id: string
          is_remote: boolean | null
          location: string | null
          requirements: string[] | null
          salary_max: number | null
          salary_min: number | null
          skills_needed: string[] | null
          status: string | null
          title: string
          type: string
          updated_at: string | null
        }
        Insert: {
          category?: string | null
          contact_info?: string | null
          contact_method?: string | null
          created_at?: string | null
          deadline?: string | null
          description: string
          employer_id: string
          id?: string
          is_remote?: boolean | null
          location?: string | null
          requirements?: string[] | null
          salary_max?: number | null
          salary_min?: number | null
          skills_needed?: string[] | null
          status?: string | null
          title: string
          type: string
          updated_at?: string | null
        }
        Update: {
          category?: string | null
          contact_info?: string | null
          contact_method?: string | null
          created_at?: string | null
          deadline?: string | null
          description?: string
          employer_id?: string
          id?: string
          is_remote?: boolean | null
          location?: string | null
          requirements?: string[] | null
          salary_max?: number | null
          salary_min?: number | null
          skills_needed?: string[] | null
          status?: string | null
          title?: string
          type?: string
          updated_at?: string | null
        }
        Relationships: []
      }
      phone_user_mapping: {
        Row: {
          created_at: string
          id: string
          phone_number: string
          updated_at: string
          user_id: string
        }
        Insert: {
          created_at?: string
          id?: string
          phone_number: string
          updated_at?: string
          user_id: string
        }
        Update: {
          created_at?: string
          id?: string
          phone_number?: string
          updated_at?: string
          user_id?: string
        }
        Relationships: []
      }
      profiles: {
        Row: {
          age: number | null
          created_at: string | null
          email: string | null
          full_name: string
          gender: string | null
          id: string
          is_pwd: boolean | null
          is_rural: boolean | null
          location: string | null
          phone: string | null
          preferred_language: string | null
          updated_at: string | null
          user_id: string
        }
        Insert: {
          age?: number | null
          created_at?: string | null
          email?: string | null
          full_name: string
          gender?: string | null
          id?: string
          is_pwd?: boolean | null
          is_rural?: boolean | null
          location?: string | null
          phone?: string | null
          preferred_language?: string | null
          updated_at?: string | null
          user_id: string
        }
        Update: {
          age?: number | null
          created_at?: string | null
          email?: string | null
          full_name?: string
          gender?: string | null
          id?: string
          is_pwd?: boolean | null
          is_rural?: boolean | null
          location?: string | null
          phone?: string | null
          preferred_language?: string | null
          updated_at?: string | null
          user_id?: string
        }
        Relationships: []
      }
      skills: {
        Row: {
          created_at: string | null
          description: string | null
          experience_years: number | null
          hourly_rate: number | null
          id: string
          is_available: boolean | null
          portfolio_images: string[] | null
          skill_category: string
          skill_name: string
          user_id: string
        }
        Insert: {
          created_at?: string | null
          description?: string | null
          experience_years?: number | null
          hourly_rate?: number | null
          id?: string
          is_available?: boolean | null
          portfolio_images?: string[] | null
          skill_category: string
          skill_name: string
          user_id: string
        }
        Update: {
          created_at?: string | null
          description?: string | null
          experience_years?: number | null
          hourly_rate?: number | null
          id?: string
          is_available?: boolean | null
          portfolio_images?: string[] | null
          skill_category?: string
          skill_name?: string
          user_id?: string
        }
        Relationships: []
      }
      user_roles: {
        Row: {
          id: string
          role: Database["public"]["Enums"]["app_role"]
          user_id: string
        }
        Insert: {
          id?: string
          role: Database["public"]["Enums"]["app_role"]
          user_id: string
        }
        Update: {
          id?: string
          role?: Database["public"]["Enums"]["app_role"]
          user_id?: string
        }
        Relationships: []
      }
      ussd_interactions: {
        Row: {
          created_at: string | null
          id: string
          interaction_type: string | null
          phone_number: string
          response_sent: string | null
          session_id: string | null
          user_input: string | null
        }
        Insert: {
          created_at?: string | null
          id?: string
          interaction_type?: string | null
          phone_number: string
          response_sent?: string | null
          session_id?: string | null
          user_input?: string | null
        }
        Update: {
          created_at?: string | null
          id?: string
          interaction_type?: string | null
          phone_number?: string
          response_sent?: string | null
          session_id?: string | null
          user_input?: string | null
        }
        Relationships: []
      }
    }
    Views: {
      [_ in never]: never
    }
    Functions: {
      has_role: {
        Args: {
          _role: Database["public"]["Enums"]["app_role"]
          _user_id: string
        }
        Returns: boolean
      }
    }
    Enums: {
      app_role: "youth" | "employer" | "ngo" | "government" | "admin"
    }
    CompositeTypes: {
      [_ in never]: never
    }
  }
}

type DatabaseWithoutInternals = Omit<Database, "__InternalSupabase">

type DefaultSchema = DatabaseWithoutInternals[Extract<keyof Database, "public">]

export type Tables<
  DefaultSchemaTableNameOrOptions extends
    | keyof (DefaultSchema["Tables"] & DefaultSchema["Views"])
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof (DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"] &
        DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Views"])
    : never = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? (DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"] &
      DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Views"])[TableName] extends {
      Row: infer R
    }
    ? R
    : never
  : DefaultSchemaTableNameOrOptions extends keyof (DefaultSchema["Tables"] &
        DefaultSchema["Views"])
    ? (DefaultSchema["Tables"] &
        DefaultSchema["Views"])[DefaultSchemaTableNameOrOptions] extends {
        Row: infer R
      }
      ? R
      : never
    : never

export type TablesInsert<
  DefaultSchemaTableNameOrOptions extends
    | keyof DefaultSchema["Tables"]
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"]
    : never = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Insert: infer I
    }
    ? I
    : never
  : DefaultSchemaTableNameOrOptions extends keyof DefaultSchema["Tables"]
    ? DefaultSchema["Tables"][DefaultSchemaTableNameOrOptions] extends {
        Insert: infer I
      }
      ? I
      : never
    : never

export type TablesUpdate<
  DefaultSchemaTableNameOrOptions extends
    | keyof DefaultSchema["Tables"]
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"]
    : never = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Update: infer U
    }
    ? U
    : never
  : DefaultSchemaTableNameOrOptions extends keyof DefaultSchema["Tables"]
    ? DefaultSchema["Tables"][DefaultSchemaTableNameOrOptions] extends {
        Update: infer U
      }
      ? U
      : never
    : never

export type Enums<
  DefaultSchemaEnumNameOrOptions extends
    | keyof DefaultSchema["Enums"]
    | { schema: keyof DatabaseWithoutInternals },
  EnumName extends DefaultSchemaEnumNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaEnumNameOrOptions["schema"]]["Enums"]
    : never = never,
> = DefaultSchemaEnumNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaEnumNameOrOptions["schema"]]["Enums"][EnumName]
  : DefaultSchemaEnumNameOrOptions extends keyof DefaultSchema["Enums"]
    ? DefaultSchema["Enums"][DefaultSchemaEnumNameOrOptions]
    : never

export type CompositeTypes<
  PublicCompositeTypeNameOrOptions extends
    | keyof DefaultSchema["CompositeTypes"]
    | { schema: keyof DatabaseWithoutInternals },
  CompositeTypeName extends PublicCompositeTypeNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"]
    : never = never,
> = PublicCompositeTypeNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"][CompositeTypeName]
  : PublicCompositeTypeNameOrOptions extends keyof DefaultSchema["CompositeTypes"]
    ? DefaultSchema["CompositeTypes"][PublicCompositeTypeNameOrOptions]
    : never

export const Constants = {
  public: {
    Enums: {
      app_role: ["youth", "employer", "ngo", "government", "admin"],
    },
  },
} as const
